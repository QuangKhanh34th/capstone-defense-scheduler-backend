package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.ProjectResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(Integer semesterId, String search, String major, Pageable pageable) {

        if (semesterId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester ID is required for filtering.");
        }

        if (!semesterRepository.existsById(semesterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found with ID: " + semesterId);
        }

        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("semester").get("semesterId"), semesterId));

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), likePattern));
            }

            if (major != null && !major.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("major"), major.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);

        return projectPage.map(this::mapToResponse);
    }

    private ProjectResponse mapToResponse(Project project) {

        String supName = "N/A";
        String supCode = "N/A";
        String supEmail = "N/A";

        if (project.getProjectSupervisors() != null) {
            ProjectSupervisor mainSup = project.getProjectSupervisors().stream()
                    .filter(ps -> "MAIN".equalsIgnoreCase(ps.getRoleType()))
                    .findFirst()
                    .orElse(project.getProjectSupervisors().isEmpty() ? null : project.getProjectSupervisors().get(0));
            if (mainSup != null && mainSup.getLecturer() != null) {
                supName = mainSup.getLecturer().getFullName();
                supCode = mainSup.getLecturer().getLecturerCode();
                supEmail = mainSup.getLecturer().getEmail();
            }
        }

        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .major(project.getMajor())
                .status(project.getStatus())
                .semesterId(project.getSemester().getSemesterId())
                .semesterName(project.getSemester().getName())
                .supervisorName(supName)
                .supervisorCode(supCode)
                .supervisorEmail(supEmail)
                .build();
    }
}