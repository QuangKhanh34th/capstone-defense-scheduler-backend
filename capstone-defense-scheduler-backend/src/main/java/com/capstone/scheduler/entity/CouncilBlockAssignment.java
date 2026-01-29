package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "council_block_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"block_id", "lecturer_id"})
                // Ràng buộc QUAN TRỌNG:
                // Trong 1 Ca (Block), một Giảng viên chỉ được phân công 1 lần duy nhất.
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilBlockAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    // FOREIGN KEYS

    // Phân công vào Ca nào
    @NotNull(message = "Council Block is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private CouncilBlock councilBlock;

    // Ai được phân công
    @NotNull(message = "Lecturer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Giữ chức vụ gì
    @NotNull(message = "Council Role is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private CouncilRole councilRole;
}