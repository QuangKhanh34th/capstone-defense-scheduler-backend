package com.capstone.scheduler.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.capstone.scheduler.dto.request.SaveScheduleRequest;
import com.capstone.scheduler.dto.request.SchedulingRequest;
import com.capstone.scheduler.dto.response.LecturerAssignmentResponse;
import com.capstone.scheduler.dto.response.SavedScheduleResponse;
import com.capstone.scheduler.dto.response.SchedulingResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.LecturerCompetencyRepository;
import com.capstone.scheduler.enums.SemesterStatus;
import com.capstone.scheduler.repository.*;
import com.capstone.scheduler.solver.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service for managing lecturer scheduling to thesis defense councils
 * using Timefold Solver for constraint satisfaction optimization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final SolverManager<DefenseScheduleSolution, Integer> solverManager;
    private final SolutionManager<DefenseScheduleSolution, HardSoftScore> solutionManager;

    private final DefenseRoundRepository defenseRoundRepository;
    private final CouncilBlockRepository councilBlockRepository;
    private final LecturerRepository lecturerRepository;
    private final CouncilRoleRepository councilRoleRepository;
    private final LecturerAvailabilityRepository availabilityRepository;
    private final LecturerQuotaRepository quotaRepository;
    private final ProjectSupervisorRepository supervisorRepository;
    private final RoundProjectRepository roundProjectRepository;
    private final NotificationTriggerService notificationTriggerService;
    private final CouncilBlockAssignmentRepository assignmentRepository;
    private final SemesterRepository semesterRepository;
    private final LecturerCompetencyRepository lecturerCompetencyRepository;

    /**
     * Start the scheduling solver for a specific defense round
     */
    public SchedulingResponse startScheduling(SchedulingRequest request) {
        Integer roundId = request.getRoundId();

        // Validate round exists
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense round not found with ID: " + roundId));

        // Build the problem
        DefenseScheduleSolution problem = buildProblem(round);

        log.info("Starting scheduling for round {} with {} blocks, {} lecturers, {} assignments",
                roundId, problem.getCouncilBlocks().size(), problem.getLecturers().size(),
                problem.getAssignments().size());

        // Start solving
        SolverJob<DefenseScheduleSolution, Integer> solverJob = solverManager.solve(roundId, problem);

        try {
            // Wait for the solution (blocking)
            DefenseScheduleSolution solution = solverJob.getFinalBestSolution();
            return buildResponse(solution, round);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during solving", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during scheduling: " + e.getMessage());
        }
    }

    /**
     * Get the current status and best solution for an ongoing solve
     */
    public SchedulingResponse getSchedulingStatus(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense round not found with ID: " + roundId));

        SolverStatus status = solverManager.getSolverStatus(roundId);

        SchedulingResponse response = SchedulingResponse.builder()
                .roundId(roundId)
                .roundName(round.getRoundName())
                .solverStatus(status.name())
                .build();

        return response;
    }

    /**
     * Stop an ongoing scheduling solve
     */
    public void stopScheduling(Integer roundId) {
        solverManager.terminateEarly(roundId);
        log.info("Terminated scheduling for round {}", roundId);
    }

    /**
     * Get the saved schedule for a specific defense round from the database.
     */
    @Transactional(readOnly = true)
    public SavedScheduleResponse getSavedSchedule(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense round not found with ID: " + roundId));

        List<CouncilBlockAssignment> assignments = assignmentRepository.findByRoundId(roundId);

        if (assignments.isEmpty()) {
            return SavedScheduleResponse.builder()
                    .roundId(roundId)
                    .roundName(round.getRoundName())
                    .totalBlocks(0)
                    .totalAssignments(0)
                    .build();
        }

        List<LecturerAssignmentResponse> assignmentResponses = new ArrayList<>();
        Map<Integer, List<LecturerAssignmentResponse>> blockGroupMap = new HashMap<>();
        Set<Integer> blockIds = new HashSet<>();

        for (CouncilBlockAssignment assignment : assignments) {
            CouncilBlock block = assignment.getCouncilBlock();
            CouncilRole role = assignment.getCouncilRole();
            Lecturer lecturer = assignment.getLecturer();
            
            blockIds.add(block.getBlockId());

            LecturerAssignmentResponse resp = LecturerAssignmentResponse.builder()
                    .blockId(block.getBlockId())
                    .blockName(block.getBlockName())
                    .defenseDate(block.getDefenseDay().getDefenseDate())
                    .startTime(block.getStartTime())
                    .endTime(block.getEndTime())
                    .roleId(role.getRoleId())
                    .roleCode(role.getRoleCode())
                    .roleName(role.getRoleName())
                    .lecturerId(lecturer.getLecturerId())
                    .lecturerCode(lecturer.getLecturerCode())
                    .lecturerName(lecturer.getFullName())
                    .lecturerEmail(lecturer.getEmail())
                    .build();

            assignmentResponses.add(resp);
            blockGroupMap.computeIfAbsent(block.getBlockId(), k -> new ArrayList<>()).add(resp);
        }

        // Build block groups
        List<SavedScheduleResponse.BlockAssignmentGroup> blockGroups = blockGroupMap.entrySet().stream()
                .map(entry -> {
                    List<LecturerAssignmentResponse> blockAssignments = entry.getValue();
                    LecturerAssignmentResponse first = blockAssignments.get(0);
                    return SavedScheduleResponse.BlockAssignmentGroup.builder()
                            .blockId(entry.getKey())
                            .blockName(first.getBlockName())
                            .defenseDate(first.getDefenseDate().toString())
                            .timeSlot(first.getStartTime() + " - " + first.getEndTime())
                            .assignments(blockAssignments)
                            .build();
                })
                .sorted(Comparator.comparing(SavedScheduleResponse.BlockAssignmentGroup::getDefenseDate)
                        .thenComparing(SavedScheduleResponse.BlockAssignmentGroup::getTimeSlot))
                .toList();

        return SavedScheduleResponse.builder()
                .roundId(roundId)
                .roundName(round.getRoundName())
                .totalBlocks(blockIds.size())
                .totalAssignments(assignments.size())
                .assignments(assignmentResponses)
                .blockGroups(blockGroups)
                .build();
    }

    /**
     * Save the provided scheduling result to the database.
     * This will overwrite any existing schedule for the given round.
     *
     * @param request The schedule request containing the assignments to save.
     */
    @Transactional
    public void saveSchedulingResult(SaveScheduleRequest request) {
        Integer roundId = request.getRoundId();
        if (roundId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Round ID must be provided in the request body.");
        }

        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense round not found with ID: " + roundId));

        // 1. Validation logic: Compare provided assignments count against expected count
        List<CouncilBlock> blocks = councilBlockRepository.findByRoundId(roundId);
        int totalBlocks = blocks.size();
        int expectedRolesPerBlock = 5; // usually 5 roles per block
        int expectedTotalAssignments = totalBlocks * expectedRolesPerBlock;

        List<SaveScheduleRequest.AssignmentDto> assignmentsToSave = request.getAssignments();
        int providedAssignmentsCount = assignmentsToSave != null ? assignmentsToSave.size() : 0;

        if (providedAssignmentsCount < expectedTotalAssignments) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Incomplete schedule: expected %d assignments (%d blocks * %d roles), but received %d.",
                            expectedTotalAssignments, totalBlocks, expectedRolesPerBlock, providedAssignmentsCount));
        }

        // Set Semester status
        Semester semester = round.getSemester();
        if (semester != null && semester.getStatus() == SemesterStatus.PLANNING) {
            semester.setStatus(SemesterStatus.ON_GOING);
            semesterRepository.save(semester);
            log.info("The Semester '{}' state has been changed to ON_GOING because a schedule was saved.", semester.getName());
        }

        // Clear existing assignments for this round
        List<CouncilBlockAssignment> existingAssignments = assignmentRepository.findByRoundId(roundId);
        if (!existingAssignments.isEmpty()) {
            assignmentRepository.deleteAll(existingAssignments);
        }

        if (assignmentsToSave == null || assignmentsToSave.isEmpty()) {
            return;
        }

        // OPTIMIZATION: Collect all IDs and fetch in bulk
        Set<Integer> blockIds = new HashSet<>();
        Set<Integer> lecturerIds = new HashSet<>();
        Set<Integer> roleIds = new HashSet<>();

        for (SaveScheduleRequest.AssignmentDto assignment : assignmentsToSave) {
            if (assignment.getLecturerId() != null) {
                blockIds.add(assignment.getBlockId());
                lecturerIds.add(assignment.getLecturerId());
                roleIds.add(assignment.getRoleId());
            }
        }

        // Bulk fetch all entities
        Map<Integer, CouncilBlock> blockMap = blockIds.isEmpty() ? Collections.emptyMap() : councilBlockRepository.findAllById(blockIds)
                .stream().collect(Collectors.toMap(CouncilBlock::getBlockId, b -> b));
        Map<Integer, Lecturer> lecturerMap = lecturerIds.isEmpty() ? Collections.emptyMap() : lecturerRepository.findAllById(lecturerIds)
                .stream().collect(Collectors.toMap(Lecturer::getLecturerId, l -> l));
        Map<Integer, CouncilRole> roleMap = roleIds.isEmpty() ? Collections.emptyMap() : councilRoleRepository.findAllById(roleIds)
                .stream().collect(Collectors.toMap(CouncilRole::getRoleId, r -> r));

        // Build assignments from maps (no DB calls inside loop)
        List<CouncilBlockAssignment> newAssignments = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (SaveScheduleRequest.AssignmentDto assignment : assignmentsToSave) {
            if (assignment.getLecturerId() != null) {
                CouncilBlockAssignment dbAssignment = new CouncilBlockAssignment();

                CouncilBlock block = blockMap.get(assignment.getBlockId());
                Lecturer lecturer = lecturerMap.get(assignment.getLecturerId());
                CouncilRole role = roleMap.get(assignment.getRoleId());

                if (block == null || lecturer == null || role == null) {
                    String errorMsg = String.format("Missing entity for assignment: blockId=%d (found: %b), lecturerId=%d (found: %b), roleId=%d (found: %b)",
                            assignment.getBlockId(), block != null,
                            assignment.getLecturerId(), lecturer != null,
                            assignment.getRoleId(), role != null);
                    log.warn(errorMsg);
                    errorMessages.add(errorMsg);
                    continue;
                }

                dbAssignment.setCouncilBlock(block);
                dbAssignment.setLecturer(lecturer);
                dbAssignment.setCouncilRole(role);
                newAssignments.add(dbAssignment);
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Failed to save schedule due to invalid IDs. Errors: " + String.join("; ", errorMessages));
        }

        // Batch save all assignments at once
        if (!newAssignments.isEmpty()) {
            assignmentRepository.saveAll(newAssignments);
        }

        log.info("Saved {} assignments for round {}", newAssignments.size(), roundId);
    }

    /**
     * Build the Timefold problem from database entities
     */
    private DefenseScheduleSolution buildProblem(DefenseRound round) {
        Integer roundId = round.getRoundId();

        // 1. Get all council blocks for this round
        List<CouncilBlock> blocks = councilBlockRepository.findByRoundId(roundId);
        if (blocks.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No council blocks found for round: " + roundId);
        }

        // 2. Get all round projects
        List<RoundProject> roundProjects = roundProjectRepository.findByRoundId(roundId);

        // OPTIMIZATION: Extract all project IDs and fetch supervisors in ONE query
        List<Integer> projectIds = roundProjects.stream()
                .map(rp -> rp.getProject().getProjectId())
                .toList();

        // Bulk fetch all supervisors for projects in this round (avoids N+1)
        List<ProjectSupervisor> allSupervisors = projectIds.isEmpty()
                ? Collections.emptyList()
                : supervisorRepository.findByProjectIdIn(projectIds);

        // Group supervisors by Project ID in memory
        Map<Integer, List<ProjectSupervisor>> supervisorsByProjectId = allSupervisors.stream()
                .collect(Collectors.groupingBy(ps -> ps.getProject().getProjectId()));

        // Build map: BlockId -> Set<SupervisorLecturerIds>
        Map<Integer, Set<Integer>> blockProjectSupervisors = new HashMap<>();

        for (RoundProject rp : roundProjects) {
            if (rp.getRoundBlocks() != null && !rp.getRoundBlocks().isEmpty()) {

                for (RoundBlock rb : rp.getRoundBlocks()) {
                    if (rb.getCouncilBlock() != null) {
                        Integer blockId = rb.getCouncilBlock().getBlockId();
                        Integer projectId = rp.getProject().getProjectId();

                        List<ProjectSupervisor> supervisors = supervisorsByProjectId
                                .getOrDefault(projectId, Collections.emptyList());

                        Set<Integer> supervisorIds = blockProjectSupervisors
                                .computeIfAbsent(blockId, k -> new HashSet<>());
                        supervisors.forEach(s -> supervisorIds.add(s.getLecturer().getLecturerId()));
                    }
                }
            }
        }

        // 3. Convert blocks to solver domain
        List<CouncilBlockInfo> blockInfos = blocks.stream()
                .map(b -> CouncilBlockInfo.builder()
                        .blockId(b.getBlockId())
                        .blockName(b.getBlockName())
                        .defenseDate(b.getDefenseDay().getDefenseDate())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .roundId(roundId)
                        .projectSupervisorIds(blockProjectSupervisors.getOrDefault(b.getBlockId(), new HashSet<>()))
                        .build())
                .toList();

        // 4. Get all active lecturers
        List<Lecturer> lecturers = lecturerRepository.findAllActiveWithDetails();

        // Get availability and quotas
        List<LecturerAvailability> availabilities = availabilityRepository.findByRoundId(roundId);
        Map<Integer, Set<LocalDate>> lecturerAvailabilityMap = availabilities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getLecturer().getLecturerId(),
                        Collectors.mapping(LecturerAvailability::getAvailableDate, Collectors.toSet())));

        List<LecturerQuota> quotas = quotaRepository.findByRoundId(roundId);
        Map<Integer, LecturerQuota> quotaMap = quotas.stream()
                .collect(Collectors.toMap(q -> q.getLecturer().getLecturerId(), q -> q));

        // Get all competencies and group them by lecturer
        List<LecturerCompetency> competencies = lecturerCompetencyRepository.findAll();
        Map<Integer, Map<Integer, Double>> competencyMap = competencies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getLecturer().getLecturerId(),
                        Collectors.toMap(
                                c -> c.getCouncilRole().getRoleId(),
                                LecturerCompetency::getWeight
                        )
                ));

        // OPTIMIZATION: Build lecturer -> supervised projects map from already-fetched data
        // We only care about conflicts with projects in THIS round, so allSupervisors is sufficient
        Map<Integer, Set<Integer>> lecturerSupervisedProjects = new HashMap<>();
        for (ProjectSupervisor ps : allSupervisors) {
            lecturerSupervisedProjects
                    .computeIfAbsent(ps.getLecturer().getLecturerId(), k -> new HashSet<>())
                    .add(ps.getProject().getProjectId());
        }

        // 5. Convert lecturers to solver domain
        List<LecturerInfo> lecturerInfos = lecturers.stream()
                .map(l -> {
                    LecturerQuota quota = quotaMap.get(l.getLecturerId());
                    return LecturerInfo.builder()
                            .lecturerId(l.getLecturerId())
                            .lecturerCode(l.getLecturerCode())
                            .fullName(l.getFullName())
                            .email(l.getEmail())
                            .departmentId(l.getDepartment().getDepartmentId())
                            .minCouncil(quota != null ? quota.getMinCouncil() : 1)
                            .maxCouncil(quota != null ? quota.getMaxCouncil() : 7)
                            .availableDates(lecturerAvailabilityMap.getOrDefault(l.getLecturerId(), new HashSet<>()))
                            .supervisedProjectIds(lecturerSupervisedProjects.getOrDefault(l.getLecturerId(), new HashSet<>()))
                            .roleCompetencyWeights(competencyMap.getOrDefault(l.getLecturerId(), Collections.emptyMap()))
                            .build();
                })
                .toList();

        // 6. Get all council roles
        List<CouncilRole> roles = councilRoleRepository.findAll();
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No council roles found in the system. Please create council roles first.");
        }
        List<CouncilRoleInfo> roleInfos = roles.stream()
                .map(r -> CouncilRoleInfo.builder()
                        .roleId(r.getRoleId())
                        .roleCode(r.getRoleCode())
                        .roleName(r.getRoleName())
                        .priority(r.getRoleId()) // Use ID as priority
                        .build())
                .toList();

        // 7. Create assignment slots (one per block per role)
        List<LecturerAssignment> assignments = new ArrayList<>();
        long assignmentId = 1;

        for (CouncilBlockInfo block : blockInfos) {
            for (CouncilRoleInfo role : roleInfos) {
                assignments.add(LecturerAssignment.builder()
                        .id(assignmentId++)
                        .councilBlock(block)
                        .role(role)
                        .lecturer(null) // To be assigned by solver
                        .build());
            }
        }

        // 8. Build the solution
        return DefenseScheduleSolution.builder()
                .roundId(roundId)
                .roundName(round.getRoundName())
                .councilBlocks(blockInfos)
                .lecturers(lecturerInfos)
                .roles(roleInfos)
                .assignments(assignments)
                .build();
    }

    /**
     * Build response DTO from solution
     */
    private SchedulingResponse buildResponse(DefenseScheduleSolution solution, DefenseRound round) {
        List<LecturerAssignmentResponse> assignmentResponses = new ArrayList<>();
        Map<Integer, List<LecturerAssignmentResponse>> blockGroupMap = new HashMap<>();

        int assignedCount = 0;
        int unassignedCount = 0;

        for (LecturerAssignment assignment : solution.getAssignments()) {
            LecturerAssignmentResponse resp = LecturerAssignmentResponse.builder()
                    .blockId(assignment.getCouncilBlock().getBlockId())
                    .blockName(assignment.getCouncilBlock().getBlockName())
                    .defenseDate(assignment.getCouncilBlock().getDefenseDate())
                    .startTime(assignment.getCouncilBlock().getStartTime())
                    .endTime(assignment.getCouncilBlock().getEndTime())
                    .roleId(assignment.getRole().getRoleId())
                    .roleCode(assignment.getRole().getRoleCode())
                    .roleName(assignment.getRole().getRoleName())
                    .build();

            if (assignment.getLecturer() != null) {
                resp.setLecturerId(assignment.getLecturer().getLecturerId());
                resp.setLecturerCode(assignment.getLecturer().getLecturerCode());
                resp.setLecturerName(assignment.getLecturer().getFullName());
                resp.setLecturerEmail(assignment.getLecturer().getEmail());
                assignedCount++;
            } else {
                unassignedCount++;
            }

            assignmentResponses.add(resp);
            blockGroupMap.computeIfAbsent(assignment.getCouncilBlock().getBlockId(), k -> new ArrayList<>())
                    .add(resp);
        }

        // Build block groups
        List<SchedulingResponse.BlockAssignmentGroup> blockGroups = blockGroupMap.entrySet().stream()
                .map(entry -> {
                    List<LecturerAssignmentResponse> blockAssignments = entry.getValue();
                    LecturerAssignmentResponse first = blockAssignments.get(0);
                    return SchedulingResponse.BlockAssignmentGroup.builder()
                            .blockId(entry.getKey())
                            .blockName(first.getBlockName())
                            .defenseDate(first.getDefenseDate().toString())
                            .timeSlot(first.getStartTime() + " - " + first.getEndTime())
                            .assignments(blockAssignments)
                            .build();
                })
                .sorted(Comparator.comparing(SchedulingResponse.BlockAssignmentGroup::getDefenseDate)
                        .thenComparing(SchedulingResponse.BlockAssignmentGroup::getTimeSlot))
                .toList();

        Set<String> hiddenConstraints = Set.of(
                "Maximize role competency",
                "Min quota preference"
                // Add more constraints to hide
        );
        // Generate a detailed, STRUCTURED explanation of the score using SolutionManager
        SchedulingResponse.ScoreAnalysisDto structuredExplanation = null;

        if (solution.getScore() != null) {
            var explanation = solutionManager.explain(solution);

            // 1. Map Constraint Matches
            List<SchedulingResponse.ConstraintMatchDto> constraintsList = explanation.getConstraintMatchTotalMap().values().stream()
                    // ❌ THIS IS THE MAGIC LINE: Skip any constraint that is in the hidden list
                    .filter(matchTotal -> !hiddenConstraints.contains(matchTotal.getConstraintName()))
                    .map(matchTotal -> {
                        List<String> justifications = matchTotal.getConstraintMatchSet().stream()
                                .map(match -> match.getJustification().toString() + " -> " + match.getScore().toString())
                                .toList();

                        return SchedulingResponse.ConstraintMatchDto.builder()
                                .constraintName(matchTotal.getConstraintName())
                                .matchCount(matchTotal.getConstraintMatchCount())
                                .scoreImpact(matchTotal.getScore().toString())
                                .justifications(justifications)
                                .build();
                    })
                    .toList();

            // 2. Map Indictments (Who is causing the score?)
            List<SchedulingResponse.IndictmentDto> indictmentsList = explanation.getIndictmentMap().entrySet().stream()
                    .map(entry -> SchedulingResponse.IndictmentDto.builder()
                            .assignedEntity(entry.getKey().toString()) // The Lecturer/Assignment object
                            .totalScoreImpact(entry.getValue().getScore().toString())
                            .matchCount(entry.getValue().getConstraintMatchCount())
                            .build())
                    // Sort to bring the biggest impacts (positive or negative) to the top
                    .sorted((a, b) -> b.getTotalScoreImpact().compareTo(a.getTotalScoreImpact()))
                    .limit(10) // Optional: Just get top 10 to keep JSON size reasonable
                    .toList();

            structuredExplanation = SchedulingResponse.ScoreAnalysisDto.builder()
                    .totalScore(explanation.getScore().toString())
                    .constraints(constraintsList)
                    .indictments(indictmentsList)
                    .build();
        }

        return SchedulingResponse.builder()
                .roundId(solution.getRoundId())
                .roundName(solution.getRoundName())
                .solverStatus("SOLVED")
                .hardScore(solution.getScore() != null ? solution.getScore().hardScore() : 0)
                .softScore(solution.getScore() != null ? solution.getScore().softScore() : 0)
                .scoreExplanation(structuredExplanation) // Pass the structured object here
                .totalBlocks(solution.getCouncilBlocks().size())
                .totalAssignments(solution.getAssignments().size())
                .assignedCount(assignedCount)
                .unassignedCount(unassignedCount)
                .assignments(assignmentResponses)
                .blockGroups(blockGroups)
                .build();
    }
}
