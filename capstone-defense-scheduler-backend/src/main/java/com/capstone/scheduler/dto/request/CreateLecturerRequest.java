package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateLecturerRequest {

    @NotBlank(message = "Lecturer code is required")
    private String lecturerCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone must be 10-11 digits")
    private String phone;

    @NotNull(message = "Department ID is required")
    private Integer departmentId;
}