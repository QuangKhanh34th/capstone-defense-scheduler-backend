package com.capstone.scheduler.entity;

import com.capstone.scheduler.enums.RunStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduling_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Integer runId;

    // FOREIGN KEYS

    // Lần chạy này thuộc về đợt bảo vệ nào
    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // COLUMNS

    @Column(name = "run_time", nullable = false)
    @Builder.Default
    private LocalDateTime runTime = LocalDateTime.now();

    // Trạng thái: PROCESSING, COMPLETED, FAILED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RunStatus status = RunStatus.PROCESSING;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;
}