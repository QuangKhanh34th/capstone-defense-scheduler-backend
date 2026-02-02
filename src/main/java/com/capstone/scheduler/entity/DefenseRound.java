package com.capstone.scheduler.entity;

import com.capstone.scheduler.enums.RoundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "defense_rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefenseRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Integer roundId;

    // FOREIGN KEYS

    @NotNull(message = "Semester is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    // COLUMN

    @Column(name = "round_name", length = 100, nullable = false)
    @NotBlank(message = "Round name is required")
    private String roundName; // Đợt bảo vệ số 1 Tốt nghiệp Spring 2026

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoundStatus status = RoundStatus.PLANNING;

    // RELATIONSHIPS

    // Quản lý các Ngày bảo vệ
    @OneToMany(mappedBy = "defenseRound", fetch = FetchType.LAZY)
    private List<DefenseDay> defenseDays;

    // Quản lý Project đăng ký tham gia
    @OneToMany(mappedBy = "defenseRound", fetch = FetchType.LAZY)
    private List<RoundProject> roundProjects;

    // Quản lý Lịch sử chạy thuật toán
    @OneToMany(mappedBy = "defenseRound", fetch = FetchType.LAZY)
    private List<SchedulingRun> schedulingRuns;

    // Quản lý Định mức 1 giảng viên được chấm bao nhiêu slot
    @OneToMany(mappedBy = "defenseRound", fetch = FetchType.LAZY)
    private List<LecturerQuota> lecturerQuotas;
}