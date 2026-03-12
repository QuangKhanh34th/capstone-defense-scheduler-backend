package com.capstone.scheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {
    @NotBlank
    @Schema(example = "Chào anh!", description = "Tiêu đề thông báo")
    private String title;

    @NotBlank
    @Schema(example = "Hệ thống mở đăng ký lịch bảo vệ, Thầy/Cô vui lòng vào đăng ký.", description = "Nội dung thông báo")
    private String body;
}
