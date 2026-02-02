package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private Integer departmentId;
    private String name;
    private String facultyName;
}