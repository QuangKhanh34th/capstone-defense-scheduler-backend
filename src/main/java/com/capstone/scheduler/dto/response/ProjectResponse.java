package com.capstone.scheduler.dto.response;

import com.capstone.scheduler.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {

    private Integer projectId;
    private String title;
    private String major;
    private ProjectStatus status;

    private String supervisorName;
    private String supervisorCode;
    private String supervisorEmail;

    private Integer semesterId;
    private String semesterName;
}