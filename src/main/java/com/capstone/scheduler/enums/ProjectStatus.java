package com.capstone.scheduler.enums;

public enum ProjectStatus {
    PENDING,    // Đang thực hiện / Chờ bảo vệ
    COMPLETED,  // Đã bảo vệ và Đạt (Pass)
    FAILED,     // Bảo vệ trượt
    DELETED     // Xóa mềm
}