package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "defense_rounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Integer roundId;

    // Một đợt bảo vệ thuộc về 1 học kỳ
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "round_name", length = 100, nullable = false)
    @NotBlank(message = "Tên đợt bảo vệ là bắt buộc")
    private String roundName; // VD: Đợt bảo vệ Tốt nghiệp Spring 2024

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Ghi chú thêm

    // Trạng thái: PLANNED, REGISTRATION, SCHEDULING, PUBLISHED
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PLANNED";
}