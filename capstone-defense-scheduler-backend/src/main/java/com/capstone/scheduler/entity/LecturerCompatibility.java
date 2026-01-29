package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "lecturer_compatibilities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lecturer_1_id", "lecturer_2_id"})
                // Ràng buộc QUAN TRỌNG:
                // Ngăn chặn việc tạo 2 dòng trùng lặp cho cùng 1 cặp giảng viên.
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // FOREIGN KEYS

    // Giảng viên 1
    @NotNull(message = "Lecturer 1 is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_1_id", nullable = false)
    private Lecturer lecturer1;

    // Giảng viên 2
    @NotNull(message = "Lecturer 2 is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_2_id", nullable = false)
    private Lecturer lecturer2;

    // COLUMNS

    // Điểm tương thích (VD: 0.0 đến 1.0)
    // 0.0: Kỵ nhau (tránh xếp chung)
    // 1.0: Rất hợp nhau (ưu tiên xếp chung)
    @Column(name = "compatibility_score")
    @Builder.Default
    private Double compatibilityScore = 0.5; // Mặc định bình thường
}