package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request DTO for starting a scheduling run
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingRequest {

    @NotNull(message = "Defense round ID is required")
    @Positive(message = "Defense round ID must be positive")
    private Integer roundId;

    // Time limit for solving in seconds (default: 30 seconds)
    @Builder.Default
    private Integer timeLimitSeconds = 30;
}
