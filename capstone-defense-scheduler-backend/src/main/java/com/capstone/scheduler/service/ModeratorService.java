package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CompetencySetRequest;
import com.capstone.scheduler.dto.request.QuotaSetRequest;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModeratorService {

    private final LecturerQuotaRepository quotaRepository;
    private final LecturerCompetencyRepository competencyRepository;
    private final DefenseRoundRepository roundRepository;
    private final LecturerRepository lecturerRepository;
    private final CouncilRoleRepository roleRepository;

    // --- CHỨC NĂNG 1: SET QUOTA (BR-26) ---
    @Transactional
    public List<LecturerQuota> setLecturerQuotas(Integer roundId, QuotaSetRequest request) {

        // 1. Check Round tồn tại
        DefenseRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Round not found"));

        List<LecturerQuota> savedQuotas = new ArrayList<>();

        for (QuotaSetRequest.LecturerQuotaDTO dto : request.getQuotas()) {
            // 2. Validate Logic Min > Max
            if (dto.getMinCouncil() > dto.getMaxCouncil()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Logic Error: Min Council (" + dto.getMinCouncil() +
                                ") cannot be greater than Max Council (" + dto.getMaxCouncil() + ") for lecturer " + dto.getLecturerId());
            }

            // 3. Check Lecturer tồn tại
            Lecturer lecturer = lecturerRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Lecturer not found ID: " + dto.getLecturerId()));

            // 4. Upsert (Có rồi thì Update, chưa thì Insert)
            LecturerQuota quota = quotaRepository
                    .findByDefenseRound_RoundIdAndLecturer_LecturerId(roundId, dto.getLecturerId())
                    .orElse(new LecturerQuota());

            // Set giá trị mới
            if (quota.getQuotaId() == null) { // Nếu là mới
                quota.setDefenseRound(round);
                quota.setLecturer(lecturer);
            }
            quota.setMinCouncil(dto.getMinCouncil());
            quota.setMaxCouncil(dto.getMaxCouncil());

            savedQuotas.add(quotaRepository.save(quota));
        }
        return savedQuotas;
    }

    // --- CHỨC NĂNG 2: SET COMPETENCY (BR-21, BR-22) ---
    @Transactional
    public List<LecturerCompetency> setLecturerCompetencies(Integer lecturerId, CompetencySetRequest request) {

        // 1. Check Lecturer tồn tại
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found"));

        List<LecturerCompetency> savedCompetencies = new ArrayList<>();

        for (CompetencySetRequest.RoleScoreDTO dto : request.getCompetencies()) {

            // 2. Check Role tồn tại
            CouncilRole role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Council Role not found ID: " + dto.getRoleId()));

            // 3. Upsert (Tìm xem GV này đã có điểm cho Role này chưa)
            LecturerCompetency competency = competencyRepository
                    .findByLecturer_LecturerIdAndCouncilRole_RoleId(lecturerId, dto.getRoleId())
                    .orElse(new LecturerCompetency());

            if (competency.getId() == null) {
                competency.setLecturer(lecturer);
                competency.setCouncilRole(role);
            }

            // 4. Set điểm (Weight)
            competency.setWeight(dto.getScore());

            savedCompetencies.add(competencyRepository.save(competency));
        }

        return savedCompetencies;
    }
}