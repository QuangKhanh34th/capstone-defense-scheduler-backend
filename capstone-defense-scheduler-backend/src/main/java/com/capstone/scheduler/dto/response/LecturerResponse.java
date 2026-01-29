package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LecturerResponse {
    private Integer lecturerId;
    private String fullName;
    private String email;
    private String lecturerCode;
    private String phone;
    private String departmentName;

    private Double scorePresident;
    private Double scoreSecretary;
    private Double scoreBusiness;
    private Double scoreTech;
    private Double scoreAI;

    private Integer minQuota;
    private Integer maxQuota;
}