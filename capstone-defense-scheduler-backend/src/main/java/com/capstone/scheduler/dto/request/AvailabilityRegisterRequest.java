package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AvailabilityRegisterRequest {

    @NotNull(message = "Lecturer ID is required")
    private Integer lecturerId;

    @NotNull(message = "Round ID is required")
    private Integer roundId;

    @NotEmpty(message = "Please select at least one date")
    private List<LocalDate> availableDates;
}