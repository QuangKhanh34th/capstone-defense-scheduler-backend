package com.capstone.scheduler.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.capstone.scheduler.dto.request.SchedulingRequest;
import com.capstone.scheduler.dto.response.LecturerAssignmentResponse;
import com.capstone.scheduler.dto.response.SchedulingResponse;
import com.capstone.scheduler.entity.*;
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

    private final DefenseRoundRepository defenseRoundRepository;
    private final CouncilBlockRepository councilBlockRepository;
    private final LecturerRepository lecturerRepository;
    private final CouncilRoleRepository councilRoleRepository;
    private final LecturerAvailabilityRepository availabilityRepository;
    private final LecturerQuotaRepository quotaRepository;
    private final ProjectSupervisorRepository supervisorRepository;
    private final RoundProjectRepository roundProjectRepository;
    private final CouncilBlockAssignmentRepository assignmentRepository;

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
     * Save the scheduling result to database
     */
    @Transactional
    public SchedulingResponse saveSchedulingResult(Integer roundId) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense round not found with ID: " + roundId));

        // Get current solution
        DefenseScheduleSolution problem = buildProblem(round);

        try {
            SolverJob<DefenseScheduleSolution, Integer> solverJob = solverManager.solve(roundId, problem);
            DefenseScheduleSolution solution = solverJob.getFinalBestSolution();

            // Clear existing assignments for this round
            List<CouncilBlockAssignment> existingAssignments = assignmentRepository.findByRoundId(roundId);
            assignmentRepository.deleteAll(existingAssignments);

            // Save new assignments
            for (LecturerAssignment assignment : solution.getAssignments()) {
                if (assignment.getLecturer() != null) {
                    CouncilBlockAssignment dbAssignment = new CouncilBlockAssignment();

                    CouncilBlock block = councilBlockRepository.findById(assignment.getCouncilBlock().getBlockId())
                            .orElseThrow();
                    Lecturer lecturer = lecturerRepository.findById(assignment.getLecturer().getLecturerId())
                            .orElseThrow();
                    CouncilRole role = councilRoleRepository.findById(assignment.getRole().getRoleId())
                            .orElseThrow();

                    dbAssignment.setCouncilBlock(block);
                    dbAssignment.setLecturer(lecturer);
                    dbAssignment.setCouncilRole(role);

                    assignmentRepository.save(dbAssignment);
                }
            }

            log.info("Saved scheduling result for round {}", roundId);
            return buildResponse(solution, round);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving scheduling result", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error saving scheduling result: " + e.getMessage());
        }
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

        // 2. Get all round projects and their supervisors
        List<RoundProject> roundProjects = roundProjectRepository.findByRoundId(roundId);
        Map<Integer, Set<Integer>> blockProjectSupervisors = new HashMap<>();

        for (RoundProject rp : roundProjects) {
            if (rp.getCouncil() != null && rp.getCouncil().getCouncilBlock() != null) {
                Integer blockId = rp.getCouncil().getCouncilBlock().getBlockId();
                List<ProjectSupervisor> supervisors = supervisorRepository
                        .findByProjectId(rp.getProject().getProjectId());

                Set<Integer> supervisorIds = blockProjectSupervisors
                        .computeIfAbsent(blockId, k -> new HashSet<>());
                supervisors.forEach(s -> supervisorIds.add(s.getLecturer().getLecturerId()));
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
        List<Lecturer> lecturers = lecturerRepository.findByIsActiveTrue();

        // Get availability and quotas
        List<LecturerAvailability> availabilities = availabilityRepository.findByRoundId(roundId);
        Map<Integer, Set<LocalDate>> lecturerAvailabilityMap = availabilities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getLecturer().getLecturerId(),
                        Collectors.mapping(LecturerAvailability::getAvailableDate, Collectors.toSet())));

        List<LecturerQuota> quotas = quotaRepository.findByRoundId(roundId);
        Map<Integer, LecturerQuota> quotaMap = quotas.stream()
                .collect(Collectors.toMap(q -> q.getLecturer().getLecturerId(), q -> q));

        // Get supervised projects
        Map<Integer, Set<Integer>> lecturerSupervisedProjects = new HashMap<>();
        for (Lecturer l : lecturers) {
            List<ProjectSupervisor> supervised = supervisorRepository.findByLecturerId(l.getLecturerId());
            Set<Integer> projectIds = supervised.stream()
                    .map(s -> s.getProject().getProjectId())
                    .collect(Collectors.toSet());
            lecturerSupervisedProjects.put(l.getLecturerId(), projectIds);
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
                            .build();
                })
                .toList();

        // 6. Get all council roles
        List<CouncilRole> roles = councilRoleRepository.findAll();
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

        return SchedulingResponse.builder()
                .roundId(solution.getRoundId())
                .roundName(solution.getRoundName())
                .solverStatus("SOLVED")
                .hardScore(solution.getScore() != null ? solution.getScore().hardScore() : 0)
                .softScore(solution.getScore() != null ? solution.getScore().softScore() : 0)
                .scoreExplanation(solution.getScore() != null ? solution.getScore().toString() : "N/A")
                .totalBlocks(solution.getCouncilBlocks().size())
                .totalAssignments(solution.getAssignments().size())
                .assignedCount(assignedCount)
                .unassignedCount(unassignedCount)
                .assignments(assignmentResponses)
                .blockGroups(blockGroups)
                .build();
    }
}
