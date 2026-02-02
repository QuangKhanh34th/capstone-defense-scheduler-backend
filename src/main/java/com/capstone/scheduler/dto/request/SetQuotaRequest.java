package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetQuotaRequest {

    @NotNull(message = "Lecturer ID is required")
    private Integer lecturerId;

    @Min(value = 0, message = "Min council cannot be negative")
    private Integer minCouncil;

    @Min(value = 0, message = "Max council cannot be negative")
    private Integer maxCouncil;
}