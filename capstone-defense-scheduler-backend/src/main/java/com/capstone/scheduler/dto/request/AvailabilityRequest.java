package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityRequest {

    @NotNull(message = "Lecturer ID is required")
    private Integer lecturerId;

    @NotNull(message = "Round ID is required")
    private Integer roundId;

    @NotNull(message = "Available date is required")
    private LocalDate availableDate;
}
