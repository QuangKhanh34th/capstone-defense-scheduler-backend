package com.capstone.scheduler.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProjectImportRequest {

    @NotNull(message = "Semester ID is required")
    private Integer semesterId;

    @NotEmpty(message = "Project list must not be empty")
    @Valid
    private List<ProjectDTO> projects;

    @Data
    public static class ProjectDTO {
        @NotBlank(message = "Project title is required")
        private String title;

        private String major;

        @NotEmpty(message = "Supervisor names list must not be empty")
        private List<String> supervisorNames;
    }
}