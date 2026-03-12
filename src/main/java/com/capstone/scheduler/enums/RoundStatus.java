package com.capstone.scheduler.enums;

public enum RoundStatus {
    PLANNING,   // Lên kế hoạch
    ON_GOING,   // Đang diễn ra, chuyển sang khi ngày hiện tại là defense day đầu tiên trong list DefenseDay của DefenseRound
    COMPLETED,  // Kết thúc, chuyển sang khi ngày hiện tại là defense day cuối cùng trong list DefenseDay của DefenseRound
    CANCELLED   // Hủy bỏ, chuyển sang khi user nhấn hủy (chỉ có thể hủy khi round status là planning)
}