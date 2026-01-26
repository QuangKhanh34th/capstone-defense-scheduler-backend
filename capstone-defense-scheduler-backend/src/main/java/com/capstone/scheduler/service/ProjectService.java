package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.ProjectImportRequest;
import com.capstone.scheduler.dto.response.ProjectResponse;
import com.capstone.scheduler.entity.*;
import com.capstone.scheduler.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectSupervisorRepository projectSupervisorRepository;
    private final SemesterRepository semesterRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional(rollbackFor = Exception.class) // Lỗi là hoàn tác toàn bộ
    public List<ProjectResponse> importProjects(ProjectImportRequest request) {

        // 1. Kiểm tra Semester (BR-02)
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found"));

        List<ProjectResponse> responseList = new ArrayList<>();

        // 2. Duyệt qua từng Project
        for (ProjectImportRequest.ProjectDTO projectDto : request.getProjects()) {

            // A. Lưu Project vào DB
            Project project = new Project();
            project.setSemester(semester);
            project.setTitle(projectDto.getTitle());
            project.setMajor(projectDto.getMajor());
            project.setStatus("PENDING"); // BR-03

            Project savedProject = projectRepository.save(project);

            // B. Xử lý Supervisors (Dựa trên TÊN)
            List<String> names = projectDto.getSupervisorNames();
            List<ProjectResponse.SupervisorDTO> supervisorDTOS = new ArrayList<>();

            for (int i = 0; i < names.size(); i++) {
                String fullName = names.get(i).trim(); // Xóa khoảng trắng thừa

                // Tìm giảng viên (Không phân biệt hoa thường)
                Lecturer lecturer = lecturerRepository.findByFullNameIgnoreCase(fullName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Lecturer not found: '" + fullName + "' in project '" + projectDto.getTitle() + "'"
                        ));

                // Tạo liên kết Project - Lecturer
                ProjectSupervisor supervisor = new ProjectSupervisor();
                supervisor.setProject(savedProject);
                supervisor.setLecturer(lecturer);

                // Người đầu tiên là MAIN, còn lại là CO
                String role = (i == 0) ? "MAIN" : "CO";
                supervisor.setRoleType(role);

                projectSupervisorRepository.save(supervisor);

                // Thêm vào list để trả về Frontend xem
                supervisorDTOS.add(ProjectResponse.SupervisorDTO.builder()
                        .lecturerName(lecturer.getFullName())
                        .role(role)
                        .build());
            }

            // C. Tạo Response cho Project này
            responseList.add(ProjectResponse.builder()
                    .projectId(savedProject.getProjectId())
                    .title(savedProject.getTitle())
                    .major(savedProject.getMajor())
                    .semesterName(semester.getName())
                    .status(savedProject.getStatus())
                    .supervisors(supervisorDTOS)
                    .build());
        }

        return responseList;
    }
}