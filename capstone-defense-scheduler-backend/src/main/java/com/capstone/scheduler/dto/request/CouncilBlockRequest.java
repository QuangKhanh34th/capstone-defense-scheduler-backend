package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

@Data
public class CouncilBlockRequest {

    @NotBlank(message = "Tên ca bảo vệ không được để trống")
    private String blockName;

    @NotNull(message = "Giờ bắt đầu là bắt buộc")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc là bắt buộc")
    private LocalTime endTime;

    @Min(value = 1, message = "Số lượng nhóm dự kiến phải ít nhất là 1")
    private Integer expectedProjectCount;
}