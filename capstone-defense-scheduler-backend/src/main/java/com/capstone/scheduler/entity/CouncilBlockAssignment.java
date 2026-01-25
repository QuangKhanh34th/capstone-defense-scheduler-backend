package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "council_block_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilBlockAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;


    // 1. Phân công vào Ca nào?
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    private CouncilBlock councilBlock;

    // 2. Ai được phân công?
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // 3. Giữ chức vụ gì?
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private CouncilRole councilRole;
}