package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.CouncilBlockResponse;
import com.capstone.scheduler.entity.*;
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

    @Transactional
    public List<CouncilBlockResponse> autoCreateBlocksForDay(Integer dayId) {

        DefenseDay day = defenseDayRepository.findById(dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Day not found with ID: " + dayId));

        Integer roundId = day.getDefenseRound().getRoundId();

        List<RoundProject> unassignedProjects = roundProjectRepository.findByDefenseRound_RoundIdAndRoundBlockIsNull(roundId);

        if (unassignedProjects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No unassigned projects found in this Round.");
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

            RoundBlock roundBlock = RoundBlock.builder()
                    .councilBlock(councilBlock)
                    .build();
            roundBlock = roundBlockRepository.save(roundBlock);

            for (RoundProject rp : batch) {
                rp.setRoundBlock(roundBlock);
                rp.setResultStatus("ASSIGNED_TO_BLOCK");
            }
            roundProjectRepository.saveAll(batch);

            responses.add(CouncilBlockResponse.builder()
                    .blockId(councilBlock.getBlockId())
                    .blockName(councilBlock.getBlockName())
                    .roundBlockId(roundBlock.getRoundBlockId())
                    .projectCount(batchSize)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());

            startIndex += MAX_PROJECTS;
        }

        return responses;
    }

    private LocalTime calculateEndTime(LocalTime start, int projectCount) {
        if (projectCount <= 0) return start;

        int totalDefenseMinutes = projectCount * MINUTES_PER_PROJECT;

        int totalBreakMinutes = (projectCount - 1) * MINUTES_BREAK;
        if (totalBreakMinutes < 0) totalBreakMinutes = 0;

        return start.plusMinutes(totalDefenseMinutes + totalBreakMinutes);
    }
}