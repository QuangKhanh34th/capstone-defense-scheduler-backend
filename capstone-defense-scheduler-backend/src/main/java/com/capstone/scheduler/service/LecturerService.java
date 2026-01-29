package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.LecturerResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.LecturerRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;

    @Transactional(readOnly = true)
    public Page<LecturerResponse> getLecturers(String keyword, Integer departmentId, Integer roundId, Pageable pageable) {

        // Tạo điều kiện lọc
        Specification<Lecturer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo keyword
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likePattern),
                        cb.like(cb.lower(root.get("lecturerCode")), likePattern)
                ));
            }

            // Lọc theo Department
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), departmentId));
            }

            predicates.add(cb.equal(root.get("isActive"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Lecturer> pageResult = lecturerRepository.findAll(spec, pageable);

        return pageResult.map(lecturer -> mapToResponse(lecturer, roundId));
    }

    private LecturerResponse mapToResponse(Lecturer l, Integer roundId) {
        LecturerResponse.LecturerResponseBuilder builder = LecturerResponse.builder()
                .lecturerId(l.getLecturerId())
                .fullName(l.getFullName())
                .email(l.getEmail())
                .lecturerCode(l.getLecturerCode())
                .phone(l.getPhone())
                .departmentName(l.getDepartment().getName());

        // Map Competency
        if (l.getCompetencies() != null) {
            Map<String, Double> compMap = l.getCompetencies().stream()
                    .collect(Collectors.toMap(
                            c -> c.getCouncilRole().getRoleCode(),
                            LecturerCompetency::getWeight,
                            (existing, replacement) -> existing
                    ));

            builder.scorePresident(compMap.getOrDefault("PRESIDENT", 0.0));
            builder.scoreSecretary(compMap.getOrDefault("SECRETARY", 0.0));
            builder.scoreBusiness(compMap.getOrDefault("BUSINESS", 0.0));
            builder.scoreTech(compMap.getOrDefault("TECH", 0.0));
            builder.scoreAI(compMap.getOrDefault("AI", 0.0));
        }

        if (roundId != null && l.getQuotas() != null) {
            l.getQuotas().stream()
                    .filter(q -> q.getDefenseRound().getRoundId().equals(roundId))
                    .findFirst()
                    .ifPresent(q -> {
                        builder.minQuota(q.getMinCouncil());
                        builder.maxQuota(q.getMaxCouncil());
                    });
        }

        return builder.build();
    }
}