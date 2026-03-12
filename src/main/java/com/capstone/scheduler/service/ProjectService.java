package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.ProjectResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SemesterRepository semesterRepository;
    private final RoundProjectRepository roundProjectRepository;

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

    @Transactional
    public void softDeleteProject(Integer projectId) {
        // 1. Tìm đề tài (Vì có @SQLRestriction, nếu nó đã bị xóa từ trước, hàm này sẽ báo NOT_FOUND luôn)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found with ID: " + projectId));

        // 2. VALIDATE: Tuyệt đối không xóa Đề tài đã tốt nghiệp (Bảo vệ tính toàn vẹn hồ sơ điểm)
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot delete a COMPLETED project. Historical grading records must be preserved.");
        }

        // 3. GỠ LIÊN KẾT: Xóa toàn bộ dữ liệu đăng ký bảo vệ (RoundProject) của đề tài này.
        // Điều này đảm bảo thuật toán Scheduling sẽ không bốc nhầm, và Hội đồng thi sẽ không thấy nhóm "Ma".
        roundProjectRepository.deleteByProject_ProjectId(projectId);
        log.info("Detached Project ID {} from all active Defense Rounds.", projectId);

        // (Tùy chọn): Nếu bạn muốn gỡ luôn cả Giảng viên hướng dẫn, gọi projectSupervisorRepository.deleteByProject_ProjectId(projectId);
        // Tuy nhiên, thường thì giữ lại Supervisor cũng không sao vì Project đã bị ẩn đi rồi.

        // 4. SOFT DELETE: Khóa sổ đề tài
        project.setStatus(ProjectStatus.DELETED);
        projectRepository.save(project);

        log.info("Successfully soft-deleted Project ID {}.", projectId);
    }
}