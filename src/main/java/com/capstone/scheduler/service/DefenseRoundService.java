package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.DefenseRoundRequest;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.enums.RoundStatus; // IMPORT ENUM
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
                .status(RoundStatus.PLANNING)
                .build();

        DefenseRound savedRound = defenseRoundRepository.save(defenseRound);

        return mapToResponse(savedRound);
    }

    @Transactional(readOnly = true)
    public Page<DefenseRoundResponse> getDefenseRounds(Integer semesterId, Pageable pageable) {
        Specification<DefenseRound> spec = (root, query, cb) -> {
            if (semesterId != null) {
                return cb.equal(root.get("semester").get("semesterId"), semesterId);
            }
            return cb.conjunction();
        };

        Page<DefenseRound> pageResult = defenseRoundRepository.findAll(spec, pageable);
        return pageResult.map(this::mapToResponse);
    }

    private DefenseRoundResponse mapToResponse(DefenseRound round) {
        return DefenseRoundResponse.builder()
                .roundId(round.getRoundId())
                .roundName(round.getRoundName())
                .description(round.getDescription())
                .status(round.getStatus())
                .semesterId(round.getSemester().getSemesterId())
                .semesterName(round.getSemester().getName())
                .build();
    }
}