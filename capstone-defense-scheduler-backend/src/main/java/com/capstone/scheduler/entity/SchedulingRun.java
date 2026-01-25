package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduling_runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Integer runId;

    // Lần chạy này thuộc về đợt bảo vệ nào
    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    @Column(name = "run_time", nullable = false)
    private LocalDateTime runTime = LocalDateTime.now(); // Tự động lấy giờ hiện tại

    // Trạng thái: PROCESSING, COMPLETED, FAILED
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PROCESSING";

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog; // Lưu lỗi nếu thuật toán bị crash
}