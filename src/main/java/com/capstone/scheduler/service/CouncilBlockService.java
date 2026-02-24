package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AssignProjectsToBlockRequest;
import com.capstone.scheduler.dto.response.BlockProjectResponse;
import com.capstone.scheduler.dto.response.CouncilBlockDetailResponse;
import com.capstone.scheduler.dto.response.CouncilBlockResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundProjectStatus;
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouncilBlockService {

    private final DefenseDayRepository defenseDayRepository;
    private final RoundProjectRepository roundProjectRepository;
    private final CouncilBlockRepository councilBlockRepository;
    private final RoundBlockRepository roundBlockRepository;

    private static final int MAX_PROJECTS = 7;
    private static final int MINUTES_PER_PROJECT = 90;
    private static final int MINUTES_BREAK = 10;
    private static final LocalTime START_TIME_DEFAULT = LocalTime.of(7, 30);

    // AUTO CREATE BLOCKS
    @Transactional
    public List<CouncilBlockResponse> autoCreateBlocksForDay(Integer dayId) {

        DefenseDay day = defenseDayRepository.findById(dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Day not found with ID: " + dayId));

        Integer roundId = day.getDefenseRound().getRoundId();

        List<RoundProject> unassignedProjects = roundProjectRepository.findUnassignedPendingProjects(
                roundId,
                ProjectStatus.PENDING,
                RoundProjectStatus.IN_PROGRESS
        );

        if (unassignedProjects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No PENDING projects found in this Round to schedule.");
        }

        List<CouncilBlockResponse> responses = new ArrayList<>();
        int currentCount = councilBlockRepository.countByDefenseDay_DayId(dayId);

        int totalProjects = unassignedProjects.size();
        int startIndex = 0;

        while (startIndex < totalProjects) {
            int endIndex = Math.min(startIndex + MAX_PROJECTS, totalProjects);
            List<RoundProject> batch = unassignedProjects.subList(startIndex, endIndex);

            int batchSize = batch.size();
            currentCount++;

            LocalTime startTime = START_TIME_DEFAULT;
            LocalTime endTime = calculateEndTime(startTime, batchSize);

            // Tạo CouncilBlock
            CouncilBlock councilBlock = CouncilBlock.builder()
                    .defenseDay(day)
                    .blockName("Council " + currentCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .expectedProjectCount(batchSize)
                    .build();
            councilBlock = councilBlockRepository.save(councilBlock);

            // Create Assignments (RoundBlock entities)
            List<RoundBlock> assignments = new ArrayList<>();
            for (RoundProject rp : batch) {
                RoundBlock assignment = RoundBlock.builder()
                        .councilBlock(councilBlock)
                        .roundProject(rp)
                        .build();
                assignments.add(assignment);
                
                rp.setResultStatus(RoundProjectStatus.IN_PROGRESS);
            }
            roundBlockRepository.saveAll(assignments);
            roundProjectRepository.saveAll(batch);

            responses.add(CouncilBlockResponse.builder()
                    .blockId(councilBlock.getBlockId())
                    .blockName(councilBlock.getBlockName())
                    .projectCount(batchSize)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());

            startIndex += MAX_PROJECTS;
        }

        return responses;
    }

    //  MANUAL ASSIGN
    @Transactional
    public List<BlockProjectResponse> assignProjectsToBlock(Integer blockId, AssignProjectsToBlockRequest request) {

        CouncilBlock councilBlock = councilBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Council Block not found"));

        List<RoundProject> projectsToAssign = roundProjectRepository.findAllById(request.getProjectIds());

        if (projectsToAssign.size() != request.getProjectIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some Project IDs are invalid");
        }

        Integer roundIdOfBlock = councilBlock.getDefenseDay().getDefenseRound().getRoundId();
        int currentCount = councilBlock.getRoundBlocks() != null ? councilBlock.getRoundBlocks().size() : 0;
        int newCount = currentCount;

        for (RoundProject rp : projectsToAssign) {
            // Validate Round
            if (!rp.getDefenseRound().getRoundId().equals(roundIdOfBlock)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project ID " + rp.getProject().getProjectId() + " belongs to a different Round.");
            }

            if (rp.getProject().getStatus() != ProjectStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project '" + rp.getProject().getTitle() + "' is " + rp.getProject().getStatus() + ". Only PENDING projects can be assigned.");
            }

            // Check if already in this block
            boolean isAlreadyInThisBlock = false;
            if (councilBlock.getRoundBlocks() != null) {
                isAlreadyInThisBlock = councilBlock.getRoundBlocks().stream()
                        .anyMatch(rb -> rb.getRoundProject().getRoundProjectId().equals(rp.getRoundProjectId()));
            }
            
            if (!isAlreadyInThisBlock) {
                newCount++;
            }
        }

        if (newCount > MAX_PROJECTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Block capacity exceeded! Max is " + MAX_PROJECTS + ". Current: " + currentCount + ", Adding: " + (newCount - currentCount));
        }

        // Create assignments
        List<RoundBlock> newAssignments = new ArrayList<>();
        for (RoundProject rp : projectsToAssign) {
            // We allow multiple assignments, but usually not duplicates in the same block (handled by check above)
            RoundBlock assignment = RoundBlock.builder()
                    .councilBlock(councilBlock)
                    .roundProject(rp)
                    .build();
            newAssignments.add(assignment);
            rp.setResultStatus(RoundProjectStatus.IN_PROGRESS);
        }
        roundBlockRepository.saveAll(newAssignments);
        roundProjectRepository.saveAll(projectsToAssign); // Update status

        // Tính lại thời gian
        LocalTime newEndTime = calculateEndTime(councilBlock.getStartTime(), newCount);
        councilBlock.setEndTime(newEndTime);
        councilBlock.setExpectedProjectCount(newCount);
        councilBlockRepository.save(councilBlock);

        return projectsToAssign.stream()
                .map(rp -> mapProjectToDto(rp.getProject()))
                .toList();
    }

    // GET BLOCKS
    @Transactional(readOnly = true)
    public List<CouncilBlockDetailResponse> getBlocksByDayId(Integer dayId) {
        if (!defenseDayRepository.existsById(dayId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Day not found with ID: " + dayId);
        }
        List<CouncilBlock> blocks = councilBlockRepository.findByDefenseDay_DayIdOrderByBlockIdAsc(dayId);
        return blocks.stream().map(this::mapToDetailResponse).toList();
    }

    // GET PROJECTS IN BLOCK
    @Transactional(readOnly = true)
    public List<BlockProjectResponse> getProjectsInBlock(Integer blockId) {
        if (!councilBlockRepository.existsById(blockId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Council Block not found");
        }
        CouncilBlock block = councilBlockRepository.findById(blockId).orElseThrow();

        if (block.getRoundBlocks() == null) {
            return new ArrayList<>();
        }
        return block.getRoundBlocks().stream()
                .map(rb -> mapProjectToDto(rb.getRoundProject().getProject()))
                .toList();
    }


    private LocalTime calculateEndTime(LocalTime start, int projectCount) {
        if (projectCount <= 0) return start;
        int totalDefenseMinutes = projectCount * MINUTES_PER_PROJECT;
        int totalBreakMinutes = (projectCount - 1) * MINUTES_BREAK;
        if (totalBreakMinutes < 0) totalBreakMinutes = 0;
        return start.plusMinutes(totalDefenseMinutes + totalBreakMinutes);
    }

    private CouncilBlockDetailResponse mapToDetailResponse(CouncilBlock block) {
        List<BlockProjectResponse> projectDtos = new ArrayList<>();
        if (block.getRoundBlocks() != null) {
            for (RoundBlock rb : block.getRoundBlocks()) {
                if (rb.getRoundProject() != null) {
                    projectDtos.add(mapProjectToDto(rb.getRoundProject().getProject()));
                }
            }
        }
        return CouncilBlockDetailResponse.builder()
                .blockId(block.getBlockId())
                .blockName(block.getBlockName())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .currentProjectCount(projectDtos.size())
                .projects(projectDtos)
                .build();
    }

    private BlockProjectResponse mapProjectToDto(Project project) {
        String supervisorName = "N/A";
        if (project.getProjectSupervisors() != null) {
            supervisorName = project.getProjectSupervisors().stream()
                    .filter(ps -> "MAIN".equals(ps.getRoleType())) // Lưu ý: RoleType vẫn đang là String, nếu bạn đổi Enum RoleType thì sửa ở đây
                    .map(ps -> ps.getLecturer().getFullName())
                    .findFirst()
                    .orElse("N/A");
        }
        return BlockProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .major(project.getMajor())
                .supervisorName(supervisorName)
                .build();
    }
}