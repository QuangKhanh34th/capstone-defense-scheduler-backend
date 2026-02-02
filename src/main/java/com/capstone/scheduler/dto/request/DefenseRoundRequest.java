package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DefenseRoundRequest {

    @NotBlank(message = "Round name is required (e.g., Spring 2026 - Wave 1)")
    private String roundName;

    private String description;
}