package com.capstone.scheduler.enums;

public enum SemesterStatus {
    PLANNING,   // Sắp tới (Mặc định)
    ON_GOING,   // Đang diễn ra, chuyển sang khi chạy thuật toán xếp lịch cho round đầu
    FINISHED    // Đã kết thúc, chuyển sang sau khi ngày hiện tại sau end_date
}
