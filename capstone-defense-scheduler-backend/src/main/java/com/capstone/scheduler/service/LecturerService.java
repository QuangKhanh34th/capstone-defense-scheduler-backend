package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateLecturerRequest;
import com.capstone.scheduler.dto.response.LecturerResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.DepartmentRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import com.capstone.scheduler.repository.UserRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final com.capstone.scheduler.repository.CouncilBlockAssignmentRepository assignmentRepository;

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

    @Transactional
    public LecturerResponse createLecturer(CreateLecturerRequest request) {

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Department not found with ID: " + request.getDepartmentId()));

        if (userRepository.existsByUsername(request.getEmail()) || lecturerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email '" + request.getEmail() + "' is already used by another account.");
        }

        if (lecturerRepository.existsByLecturerCode(request.getLecturerCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lecturer Code '" + request.getLecturerCode() + "' already exists.");
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setPasswordHash("123456");
        user.setRole("LECTURER");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        Lecturer lecturer = Lecturer.builder()
                .user(savedUser)
                .department(department)
                .lecturerCode(request.getLecturerCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .isActive(true)
                .build();

        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        return mapToResponse(savedLecturer, null);
    }

    /**
     * Get schedule for logged-in lecturer
     */
    @Transactional(readOnly = true)
    public List<com.capstone.scheduler.dto.response.LecturerAssignmentResponse> getMySchedule(Integer roundId) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Lecturer lecturer = lecturerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer profile not found for user: " + username));

        List<CouncilBlockAssignment> assignments;
        if (roundId != null) {
            assignments = assignmentRepository.findByLecturerIdAndRoundId(lecturer.getLecturerId(), roundId);
        } else {
            assignments = assignmentRepository.findByLecturerId(lecturer.getLecturerId());
        }

        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    private com.capstone.scheduler.dto.response.LecturerAssignmentResponse mapToAssignmentResponse(CouncilBlockAssignment assignment) {
        return com.capstone.scheduler.dto.response.LecturerAssignmentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .blockId(assignment.getCouncilBlock().getBlockId())
                .blockName(assignment.getCouncilBlock().getBlockName())
                .defenseDate(assignment.getCouncilBlock().getDefenseDay().getDefenseDate())
                .startTime(assignment.getCouncilBlock().getStartTime())
                .endTime(assignment.getCouncilBlock().getEndTime())
                .lecturerId(assignment.getLecturer().getLecturerId())
                .lecturerCode(assignment.getLecturer().getLecturerCode())
                .lecturerName(assignment.getLecturer().getFullName())
                .lecturerEmail(assignment.getLecturer().getEmail())
                .roleId(assignment.getCouncilRole().getRoleId())
                .roleCode(assignment.getCouncilRole().getRoleCode())
                .roleName(assignment.getCouncilRole().getRoleName())
                .build();
    }

}