package com.capstone.scheduler.enums;

public enum ProjectStatus {
    PENDING,    // Đang thực hiện / Chờ bảo vệ
    COMPLETED,  // Chuyển sang khi RoundProjectStatus là PASSED hoặc SemesterStatus là FINISHED
    //FAILED,     // Bảo vệ trượt (bỏ qua không dùng)
    DELETED     // Xóa mềm, do user đặt
}