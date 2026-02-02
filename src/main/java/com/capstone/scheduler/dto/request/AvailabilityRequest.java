package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityRequest {

    private Integer lecturerId;

    private Integer userId;
    
    private String username; // Fallback lookup if userId is missing

    @NotNull(message = "Round ID is required")
    private Integer roundId;

    @NotNull(message = "Available date is required")
    private LocalDate availableDate;
}
