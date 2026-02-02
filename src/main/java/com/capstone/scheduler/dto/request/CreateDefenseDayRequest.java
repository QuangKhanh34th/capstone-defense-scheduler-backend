package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateDefenseDayRequest {
    @NotEmpty(message = "Date list cannot be empty")
    private List<@NotNull LocalDate> defenseDates;
}