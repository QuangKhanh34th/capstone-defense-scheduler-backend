package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "lecturer_quotas",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lecturer_id", "round_id"})
                // Ràng buộc QUAN TRỌNG:
                // Mỗi giảng viên chỉ được có 1 bản ghi định mức cho 1 đợt bảo vệ cụ thể.
                // Không thể có dòng 1: GV A - Đợt 1 - Max 5
                // Và dòng 2: GV A - Đợt 1 - Max 10
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id")
    private Integer quotaId;

    // FOREIGN KEYS

    // Chỉ tiêu của ai
    @NotNull(message = "Lecturer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Áp dụng cho đợt bảo vệ nào
    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // COLUMNS

    @Min(value = 0, message = "Min council cannot be negative")
    @Column(name = "min_council")
    @Builder.Default
    private Integer minCouncil = 0; // Tối thiểu có thể là 0

    @Min(value = 0, message = "Max council cannot be negative")
    @Column(name = "max_council")
    @Builder.Default
    private Integer maxCouncil = 7;
}