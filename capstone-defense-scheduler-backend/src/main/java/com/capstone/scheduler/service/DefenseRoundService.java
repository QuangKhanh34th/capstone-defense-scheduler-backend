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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefenseRoundService {

    private final DefenseRoundRepository defenseRoundRepository;
    private final SemesterRepository semesterRepository;


    //Create
    public DefenseRoundResponse createRound(DefenseRoundRequest request) {
        // 1. Tìm Semester
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found"));

        // 2. Lưu Entity
        DefenseRound defenseRound = new DefenseRound();
        defenseRound.setRoundName(request.getRoundName());
        defenseRound.setDescription(request.getDescription());
        defenseRound.setSemester(semester);

        DefenseRound savedRound = defenseRoundRepository.save(defenseRound);

        // 3. Convert sang Response DTO (Cắt đứt vòng lặp)
        return DefenseRoundResponse.builder()
                .roundId(savedRound.getRoundId())
                .roundName(savedRound.getRoundName())
                .description(savedRound.getDescription())
                .semesterId(semester.getSemesterId())      // Chỉ lấy ID
                .semesterName(semester.getName())          // Chỉ lấy Tên
                .status(savedRound.getStatus())
                .build();
    }


    //GetAll
    public List<DefenseRoundResponse> getAllRounds() {
        return defenseRoundRepository.findAll().stream()
                .map(round -> DefenseRoundResponse.builder()
                        .roundId(round.getRoundId())
                        .roundName(round.getRoundName())
                        .description(round.getDescription())
                        .semesterId(round.getSemester().getSemesterId())
                        .semesterName(round.getSemester().getName())
                        .status(round.getStatus())
                        .build())
                .toList();
    }
}