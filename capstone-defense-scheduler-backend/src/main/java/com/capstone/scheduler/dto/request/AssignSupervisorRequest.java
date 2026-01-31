package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssignSupervisorRequest {

    @NotBlank(message = "Lecturer Code is required")
    private String lecturerCode;

    @NotNull(message = "Role type is required")
    @Pattern(regexp = "^(MAIN|CO)$", message = "Role must be either 'MAIN' or 'CO'")
    private String roleType;
}