package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "lecturer_competencies",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lecturer_id", "role_id"})
                // Ràng buộc QUAN TRỌNG:
                // Mỗi giảng viên chỉ có 1 dòng đánh giá năng lực cho 1 vai trò nhất định.
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // FOREIGN KEYS

    @NotNull(message = "Lecturer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Phù hợp với vai trò nào
    @NotNull(message = "Council Role is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private CouncilRole councilRole;

    // COLUMNS

    // Điểm trọng số (Weight)
    // Thuật toán sẽ ưu tiên chọn người có weight cao làm Chủ tịch
    @Column(name = "weight")
    @Builder.Default
    private Double weight = 1.0;
}