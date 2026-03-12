package com.capstone.scheduler.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNotificationRequest {
    @NotBlank
    @Schema(example = "Chào anh!", description = "Tiêu đề thông báo")
    private String title;

    @NotBlank
    @Schema(example = "Đây là thông báo test từ hệ thống.", description = "Nội dung thông báo")
    private String body;

    @Schema(example = "lecturer1", description = "Username người nhận (để test theo User)")
    private String username;

    @Schema(example = "fcm_token_xyz...", description = "Token thiết bị nhận trực tiếp (để test không cần DB)")
    private String deviceToken;
}
