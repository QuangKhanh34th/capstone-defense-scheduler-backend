package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateSemesterRequest;
import com.capstone.scheduler.dto.response.SemesterResponse;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.repository.SemesterRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
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
public class SemesterService {

    private final SemesterRepository semesterRepository;

    // GET LIST
    @Transactional(readOnly = true)
    public Page<SemesterResponse> getSemesters(String keyword, String status, Pageable pageable) {

        // Tạo điều kiện lọc
        Specification<Semester> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo tên hoặc năm học (keyword)
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("schoolYear")), likePattern)
                ));
            }

            // Lọc theo trạng thái (status)
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
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