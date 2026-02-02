package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateSemesterRequest;
import com.capstone.scheduler.dto.response.SemesterResponse;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.enums.SemesterStatus; // IMPORT ENUM
import com.capstone.scheduler.repository.SemesterRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemesterService {

    private final SemesterRepository semesterRepository;

    @Transactional
    public SemesterResponse createSemester(CreateSemesterRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        if (semesterRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Semester with name '" + request.getName() + "' already exists");
        }

        Semester semester = Semester.builder()
                .name(request.getName())
                .schoolYear(request.getSchoolYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SemesterStatus.PLANNING) // FIXED: Enum
                .build();

        Semester savedSemester = semesterRepository.save(semester);
        return mapToResponse(savedSemester);
    }

    @Transactional(readOnly = true)
    public Page<SemesterResponse> getSemesters(String keyword, String statusStr, Pageable pageable) {
        Specification<Semester> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("schoolYear")), likePattern)));
            }

            // FIXED: Convert String -> Enum để search
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    SemesterStatus status = SemesterStatus.valueOf(statusStr.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Semester> pageResult = semesterRepository.findAll(spec, pageable);
        return pageResult.map(this::mapToResponse);
    }

    private SemesterResponse mapToResponse(Semester s) {
        return SemesterResponse.builder()
                .semesterId(s.getSemesterId())
                .name(s.getName())
                .schoolYear(s.getSchoolYear())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .build();
    }
}