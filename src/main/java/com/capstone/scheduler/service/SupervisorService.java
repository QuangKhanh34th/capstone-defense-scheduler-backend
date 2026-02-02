package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AssignSupervisorRequest;
import com.capstone.scheduler.dto.response.ProjectSupervisorResponse;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.Project;
import com.capstone.scheduler.entity.ProjectSupervisor;
import com.capstone.scheduler.repository.LecturerRepository;
import com.capstone.scheduler.repository.ProjectRepository;
import com.capstone.scheduler.repository.ProjectSupervisorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupervisorService {

    private final ProjectSupervisorRepository projectSupervisorRepository;
    private final ProjectRepository projectRepository;
    private final LecturerRepository lecturerRepository;

    // ASSIGN SUPERVISOR
    @Transactional
    public ProjectSupervisorResponse assignSupervisor(Integer projectId, AssignSupervisorRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found with ID: " + projectId));

        String codeInput = request.getLecturerCode().trim();
        Lecturer lecturer = lecturerRepository.findByLecturerCode(codeInput)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lecturer not found with Code: " + codeInput));

        if (projectSupervisorRepository.existsByProject_ProjectIdAndLecturer_LecturerId(projectId, lecturer.getLecturerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lecturer " + codeInput + " is already assigned to this project.");
        }

        if ("MAIN".equals(request.getRoleType())) {
            boolean hasMain = projectSupervisorRepository.existsByProject_ProjectIdAndRoleType(projectId, "MAIN");
            if (hasMain) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project already has a MAIN supervisor. Only 'CO' supervisor can be added now.");
            }
        }

        ProjectSupervisor ps = ProjectSupervisor.builder()
                .project(project)
                .lecturer(lecturer)
                .roleType(request.getRoleType())
                .build();

        ProjectSupervisor savedPs = projectSupervisorRepository.save(ps);

        return mapToResponse(savedPs);
    }

    // GET LIST OF SUPEVISTOR
    @Transactional(readOnly = true)
    public List<ProjectSupervisorResponse> getSupervisors(Integer projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + projectId);
        }
        return projectSupervisorRepository.findByProject_ProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProjectSupervisorResponse mapToResponse(ProjectSupervisor ps) {
        return ProjectSupervisorResponse.builder()
                .supervisorId(ps.getSupervisorId())
                .projectId(ps.getProject().getProjectId())
                .projectTitle(ps.getProject().getTitle())
                .lecturerId(ps.getLecturer().getLecturerId())
                .lecturerName(ps.getLecturer().getFullName())
                .lecturerCode(ps.getLecturer().getLecturerCode())
                .lecturerEmail(ps.getLecturer().getEmail())
                .roleType(ps.getRoleType())
                .build();
    }
}