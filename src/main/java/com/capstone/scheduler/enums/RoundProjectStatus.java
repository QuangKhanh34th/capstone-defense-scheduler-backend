package com.capstone.scheduler.enums;

public enum RoundProjectStatus {
    IN_PROGRESS, // Chưa có kết quả (Đang xếp lịch hoặc Đang bảo vệ)
    PASSED,      // Đậu, do user set
    FAILED       // Trượt, do user set
}