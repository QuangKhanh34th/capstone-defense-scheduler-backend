package com.capstone.scheduler.dto.request;

import com.capstone.scheduler.entity.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDeviceRequest {
    @NotBlank
    private String deviceToken;

    @NotNull
    private DeviceToken.Platform platform;
}
