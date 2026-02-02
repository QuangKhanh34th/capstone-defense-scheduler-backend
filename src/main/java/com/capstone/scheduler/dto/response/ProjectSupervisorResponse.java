package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectSupervisorResponse {
    private Integer supervisorId;

    private Integer projectId;
    private String projectTitle;

    private Integer lecturerId;
    private String lecturerName;
    private String lecturerCode;
    private String lecturerEmail;

    private String roleType;
}