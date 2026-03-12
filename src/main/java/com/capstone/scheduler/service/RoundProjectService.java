package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AddProjectToRoundRequest;
import com.capstone.scheduler.dto.response.RoundProjectResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Project;
import com.capstone.scheduler.entity.RoundProject;
import com.capstone.scheduler.enums.ProjectStatus; // MỚI: Nhớ import cái này
import com.capstone.scheduler.enums.RoundProjectStatus;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.ProjectRepository;
import com.capstone.scheduler.repository.RoundProjectRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundProjectService {

    private final DefenseRoundRepository defenseRoundRepository;
    private final ProjectRepository projectRepository;
    private final RoundProjectRepository roundProjectRepository;

    @Transactional
    public List<String> addProjectsToRound(Integer roundId, AddProjectToRoundRequest request) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        Integer roundSemesterId = round.getSemester().getSemesterId();

        List<Project> projects = projectRepository.findAllById(request.getProjectIds());

        if (projects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No projects found with provided IDs");
        }

        List<RoundProject> toSave = new ArrayList<>();
        List<String> results = new ArrayList<>();

        Set<Integer> existingProjectIds = roundProjectRepository
                .findByDefenseRound_RoundIdAndProject_ProjectIdIn(roundId, request.getProjectIds())
                .stream()
                .map(rp -> rp.getProject().getProjectId())
                .collect(Collectors.toSet());

        for (Project project : projects) {

            if (project.getStatus() != ProjectStatus.PENDING) {

                results.add("Project [" + project.getTitle() + "] - Skipped (Status is " + project.getStatus() + ", must be PENDING)");
                continue;
            }

            if (!project.getSemester().getSemesterId().equals(roundSemesterId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project [" + project.getTitle() + "] belongs to a different Semester. Cannot add to this Round.");
            }

            if (existingProjectIds.contains(project.getProjectId())) {
                results.add("Project [" + project.getTitle() + "] - Skipped (Already in round)");
                continue;
            }

            RoundProject rp = RoundProject.builder()
                    .defenseRound(round)
                    .project(project)
                    .resultStatus(RoundProjectStatus.IN_PROGRESS)
                    .build();

            toSave.add(rp);
            results.add("Project [" + project.getTitle() + "] - Added successfully");
        }

        if (!toSave.isEmpty()) {
            roundProjectRepository.saveAll(toSave);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public Page<RoundProjectResponse> getRoundProjects(
            Integer roundId,
            String searchKeyword,
            ProjectStatus projectStatus,
            String major,
            Pageable pageable) {

        // 1. TẠO BỘ LỌC ĐỘNG (DYNAMIC SPECIFICATION)
        Specification<RoundProject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn lọc theo RoundId (Bắt buộc)
            predicates.add(cb.equal(root.get("defenseRound").get("roundId"), roundId));

            // Join bảng Project để lấy các trường bên trong nó
            Join<RoundProject, Project> projectJoin = root.join("project", JoinType.INNER);

            // Filter theo Keyword (Tìm theo tên đề tài)
            if (StringUtils.hasText(searchKeyword)) {
                String likePattern = "%" + searchKeyword.toLowerCase() + "%";
                // LƯU Ý: Đổi "title" thành tên biến lưu tên đề tài trong Entity Project của bạn
                predicates.add(cb.like(cb.lower(projectJoin.get("title")), likePattern));
            }

            // Filter theo ProjectStatus
            if (projectStatus != null) {
                predicates.add(cb.equal(projectJoin.get("status"), projectStatus));
            }

            // Filter theo Major (Chuyên ngành)
            if (StringUtils.hasText(major)) {
                // LƯU Ý: Nếu Major của bạn là Entity riêng thì phải Join tiếp.
                // Ở đây giả định major là 1 cột String trong bảng Project.
                predicates.add(cb.equal(projectJoin.get("major"), major));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 2. QUERY DATABASE KÈM PHÂN TRANG VÀ SẮP XẾP
        Page<RoundProject> pageResult = roundProjectRepository.findAll(spec, pageable);

        // 3. MAP ENTITY SANG DTO
        return pageResult.map(rp -> {
            Project project = rp.getProject();

            // Xử lý lấy tên Giảng viên hướng dẫn (Có thể có nhiều GVHD nên nối chuỗi bằng dấu phẩy)
            String supervisors = "";
            if (project.getProjectSupervisors() != null) {
                supervisors = project.getProjectSupervisors().stream()
                        .map(ps -> ps.getLecturer().getFullName())
                        .collect(Collectors.joining(", "));
            }

            return RoundProjectResponse.builder()
                    .roundProjectId(rp.getRoundProjectId())
                    .projectId(project.getProjectId())
                    .projectTitle(project.getTitle()) // Thay getTitle() bằng field name của bạn
                    .projectStatus(project.getStatus() != null ? project.getStatus().name() : null)
                    .roundProjectStatus(rp.getResultStatus() != null ? rp.getResultStatus().name() : null)
                    .major(project.getMajor()) // Thay getMajor() bằng field name của bạn
                    .supervisorName(supervisors)
                    .build();
        });
    }
}