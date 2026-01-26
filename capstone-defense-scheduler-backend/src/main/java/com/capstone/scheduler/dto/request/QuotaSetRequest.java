package com.capstone.scheduler.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuotaSetRequest {

    @NotEmpty(message = "Danh sách quota không được để trống")
    @Valid
    private List<LecturerQuotaDTO> quotas;

    @Data
    public static class LecturerQuotaDTO {
        @NotNull(message = "Lecturer ID là bắt buộc")
        private Integer lecturerId;

        @Min(value = 0, message = "Min council phải >= 0")
        private Integer minCouncil;

        @Min(value = 0, message = "Max council phải >= 0")
        private Integer maxCouncil;
    }
}