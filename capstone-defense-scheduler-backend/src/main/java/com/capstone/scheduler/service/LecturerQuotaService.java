package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.SetQuotaRequest;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.LecturerQuota;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.LecturerQuotaRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerQuotaService {

    private final LecturerQuotaRepository quotaRepository;
    private final DefenseRoundRepository roundRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional
    public List<String> setLecturerQuotas(Integer roundId, List<SetQuotaRequest> requests) {

        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        List<String> results = new ArrayList<>();
        List<LecturerQuota> toSave = new ArrayList<>();

        for (SetQuotaRequest req : requests) {

            int min = (req.getMinCouncil() != null) ? req.getMinCouncil() : 0;
            int max = (req.getMaxCouncil() != null) ? req.getMaxCouncil() : 7;

            if (min > max) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lecturer ID " + req.getLecturerId() + ": Min (" + min + ") cannot be greater than Max (" + max + ")");
            }

            LecturerQuota quota = quotaRepository.findByLecturer_LecturerIdAndDefenseRound_RoundId(req.getLecturerId(), roundId)
                    .orElse(null);

            if (quota == null) {

                Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Lecturer not found with ID: " + req.getLecturerId()));

                quota = LecturerQuota.builder()
                        .defenseRound(round)
                        .lecturer(lecturer)
                        .minCouncil(min)
                        .maxCouncil(max)
                        .build();

                results.add("Created quota for Lecturer ID " + req.getLecturerId());
            } else {

                quota.setMinCouncil(min);
                quota.setMaxCouncil(max);

                results.add("Updated quota for Lecturer ID " + req.getLecturerId());
            }

            toSave.add(quota);
        }

        quotaRepository.saveAll(toSave);

        return results;
    }
}