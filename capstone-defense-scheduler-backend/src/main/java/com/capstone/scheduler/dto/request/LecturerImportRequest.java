package com.capstone.scheduler.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LecturerImportRequest {

    @NotEmpty(message = "Danh sách giảng viên không được để trống")
    @Valid
    private List<LecturerDTO> lecturers;

    @Data
    public static class LecturerDTO {
        @NotBlank(message = "Mã giảng viên không được để trống")
        private String lecturerCode;

        @NotBlank(message = "Họ tên không được để trống")
        private String fullName;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        private String phone; // Có thể null

        @NotNull(message = "ID Bộ môn là bắt buộc")
        private Integer departmentId;
    }
}