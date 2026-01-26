package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DefenseRoundRequest {

    @NotNull(message = "The semester is required.")
    private Integer semesterId;

    @NotBlank(message = "The name of the protection session must not be blank.")
    private String roundName;

    private String description;
}