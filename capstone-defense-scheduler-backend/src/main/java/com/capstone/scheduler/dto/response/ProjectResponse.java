package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Integer projectId;
    private String title;
    private String major;
    private String semesterName;
    private String status;
    private List<SupervisorDTO> supervisors;

    @Data
    @Builder
    public static class SupervisorDTO {
        private String lecturerName;
        private String role;
    }
}