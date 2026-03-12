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
import java.util.stream.Collectors;

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

            CouncilBlock councilBlock = CouncilBlock.builder()
                    .defenseDay(day)
                    .blockName("Council " + currentCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .expectedProjectCount(batchSize)
                    .build();
            councilBlock = councilBlockRepository.save(councilBlock);

            List<RoundBlock> newSlots = new ArrayList<>();
            for (RoundProject rp : batch) {
                rp.setResultStatus(RoundProjectStatus.IN_PROGRESS);

                RoundBlock slot = RoundBlock.builder()
                        .councilBlock(councilBlock)
                        .roundProject(rp)
                        .build();
                newSlots.add(slot);
            }
            roundProjectRepository.saveAll(batch);
            roundBlockRepository.saveAll(newSlots);

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
            if (!rp.getDefenseRound().getRoundId().equals(roundIdOfBlock)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project ID " + rp.getProject().getProjectId() + " belongs to a different Round.");
            }

            if (rp.getProject().getStatus() != ProjectStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project '" + rp.getProject().getTitle() + "' is " + rp.getProject().getStatus() + ". Only PENDING projects can be assigned.");
            }

            boolean alreadyInBlock = councilBlock.getRoundBlocks() != null && councilBlock.getRoundBlocks().stream()
                    .anyMatch(rb -> rb.getRoundProject() != null && rb.getRoundProject().getRoundProjectId().equals(rp.getRoundProjectId()));
            if (!alreadyInBlock) {
                newCount++;
            }
        }

        if (newCount > MAX_PROJECTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Block capacity exceeded! Max is " + MAX_PROJECTS + ". Current: " + currentCount + ", Adding: " + (newCount - currentCount));
        }

        List<RoundBlock> newSlots = new ArrayList<>();
        for (RoundProject rp : projectsToAssign) {
            rp.setResultStatus(RoundProjectStatus.IN_PROGRESS);

            if (rp.getRoundBlocks() != null && !rp.getRoundBlocks().isEmpty()) {
                roundBlockRepository.deleteAll(rp.getRoundBlocks());
            }

            RoundBlock slot = RoundBlock.builder()
                    .councilBlock(councilBlock)
                    .roundProject(rp)
                    .build();
            newSlots.add(slot);
        }
        roundProjectRepository.saveAll(projectsToAssign);
        roundBlockRepository.saveAll(newSlots);

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
        CouncilBlock councilBlock = councilBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Council Block not found"));

        if (councilBlock.getRoundBlocks() == null) {
            return new ArrayList<>();
        }

        return councilBlock.getRoundBlocks().stream()
                .filter(rb -> rb.getRoundProject() != null)
                .map(rb -> mapProjectToDto(rb.getRoundProject().getProject()))
                .collect(Collectors.toList());
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
        // MỚI: Map 1-1 từ Slot sang Project
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
                    .filter(ps -> "MAIN".equals(ps.getRoleType()))
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