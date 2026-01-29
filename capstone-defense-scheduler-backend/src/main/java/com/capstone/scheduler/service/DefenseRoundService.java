package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.DefenseRoundRequest;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DefenseRoundService {

    private final DefenseRoundRepository defenseRoundRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public DefenseRoundResponse createRound(Integer semesterId, DefenseRoundRequest request) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Semester not found with ID: " + semesterId));

        DefenseRound defenseRound = DefenseRound.builder()
                .roundName(request.getRoundName())
                .description(request.getDescription())
                .semester(semester)
                .status("PLANNED")
                .build();

        DefenseRound savedRound = defenseRoundRepository.save(defenseRound);

        return DefenseRoundResponse.builder()
                .roundId(savedRound.getRoundId())
                .roundName(savedRound.getRoundName())
                .description(savedRound.getDescription())
                .status(savedRound.getStatus())
                .semesterId(semester.getSemesterId())
                .semesterName(semester.getName())
                .build();
    }
}