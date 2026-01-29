package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSemesterRequest {

    @NotBlank(message = "Semester name is required")
    private String name;

    @NotBlank(message = "School year is required")
    private String schoolYear;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}