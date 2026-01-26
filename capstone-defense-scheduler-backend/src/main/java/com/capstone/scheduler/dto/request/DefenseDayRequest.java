package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class DefenseDayRequest {
    @NotEmpty(message = "Danh sách ngày bảo vệ không được để trống")
    private List<LocalDate> defenseDates;
}