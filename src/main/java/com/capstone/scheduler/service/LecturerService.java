package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateLecturerRequest;
import com.capstone.scheduler.dto.response.LecturerDateStatResponse;
import com.capstone.scheduler.dto.response.LecturerResponse;
import com.capstone.scheduler.dto.response.LecturerScheduleResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.CommonStatus; // IMPORT ENUM
import com.capstone.scheduler.enums.UserRole;
import com.capstone.scheduler.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.capstone.scheduler.util.PasswordGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final com.capstone.scheduler.repository.CouncilBlockAssignmentRepository assignmentRepository;
    private final LecturerAvailabilityRepository availabilityRepository;
    private final DefenseRoundRepository defenseRoundRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MIN_LECTURERS_REQUIRED = 10;

    @Transactional(readOnly = true)
    public Page<LecturerResponse> getLecturers(String keyword, Integer departmentId, Integer roundId, Pageable pageable) {

        Specification<Lecturer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), likePattern),
                        cb.like(cb.lower(root.get("lecturerCode")), likePattern)
                ));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), departmentId));
            }

            // FIXED: Lọc theo Enum CommonStatus.ACTIVE thay vì Boolean isActive
            predicates.add(cb.equal(root.get("status"), CommonStatus.ACTIVE));

            if (roundId != null) {
                // Join từ bảng Lecturer sang bảng LecturerQuota
                jakarta.persistence.criteria.Join<Lecturer, LecturerQuota> quotaJoin = root.join("quotas", jakarta.persistence.criteria.JoinType.INNER);

                // Lọc ra những ông Giảng viên có Quota nằm trong cái roundId này
                predicates.add(cb.equal(quotaJoin.get("defenseRound").get("roundId"), roundId));

                // Thêm distinct để tránh trả về dữ liệu trùng lặp khi dùng Join
                query.distinct(true);
            }

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

        String rawPassword = PasswordGenerator.generate(10);
        log.info("Generated password for lecturer {}: {}", request.getEmail(), rawPassword);

        User user = new User();
        user.setUsername(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.LECTURER);
        user.setStatus(CommonStatus.ACTIVE); // FIXED: Enum

        User savedUser = userRepository.save(user);

        Lecturer lecturer = Lecturer.builder()
                .user(savedUser)
                .department(department)
                .lecturerCode(request.getLecturerCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(CommonStatus.ACTIVE) // FIXED: Enum
                .build();

        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        return mapToResponse(savedLecturer, null);
    }

    @Transactional(readOnly = true)
    public List<LecturerScheduleResponse> getMySchedule(Integer roundId) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Lecturer lecturer = lecturerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer profile not found for user: " + username));

        if (lecturer.getStatus() != null && lecturer.getStatus() != CommonStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer account is not active.");
        }

        List<CouncilBlockAssignment> assignments;
        if (roundId != null) {
            assignments = assignmentRepository.findByLecturerIdAndRoundId(lecturer.getLecturerId(), roundId);
        } else {
            assignments = assignmentRepository.findByLecturerId(lecturer.getLecturerId());
        }

        return assignments.stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    private LecturerScheduleResponse mapToScheduleResponse(CouncilBlockAssignment assignment) {
        CouncilBlock block = assignment.getCouncilBlock();
        return LecturerScheduleResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .blockId(block.getBlockId())
                .blockName(block.getBlockName())
                .defenseDate(block.getDefenseDay().getDefenseDate())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .lecturerId(assignment.getLecturer().getLecturerId())
                .lecturerCode(assignment.getLecturer().getLecturerCode())
                .lecturerName(assignment.getLecturer().getFullName())
                .lecturerEmail(assignment.getLecturer().getEmail())
                .roleId(assignment.getCouncilRole().getRoleId())
                .roleCode(assignment.getCouncilRole().getRoleCode())
                .roleName(assignment.getCouncilRole().getRoleName())
                .build();
    }

    @Transactional(readOnly = true)
    public List<LecturerDateStatResponse> getAvailabilityStatistics(Integer roundId) {
        if (!defenseRoundRepository.existsById(roundId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Defense Round not found with ID: " + roundId);
        }
        List<Object[]> rawData = availabilityRepository.countLecturersByDate(roundId);
        List<LecturerDateStatResponse> responses = new ArrayList<>();

        for (Object[] row : rawData) {
            LocalDate date = (LocalDate) row[0];
            Long count = (Long) row[1];
            boolean isWarning = count < MIN_LECTURERS_REQUIRED;
            String message = isWarning ? "WARNING: Low turnout (" + count + "/" + MIN_LECTURERS_REQUIRED + ")" : "Sufficient capacity (" + count + ")";
            responses.add(LecturerDateStatResponse.builder()
                    .date(date).lecturerCount(count).isLowTurnout(isWarning).statusMessage(message).build());
        }
        return responses;
    }
}