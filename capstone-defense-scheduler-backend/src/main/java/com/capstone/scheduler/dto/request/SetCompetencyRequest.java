package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetCompetencyRequest {

    @NotNull(message = "Role ID is required")
    private Integer roleId;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be >= 0")
    @DecimalMax(value = "5.0", message = "Weight must be <= 5")
    private Double weight;
}