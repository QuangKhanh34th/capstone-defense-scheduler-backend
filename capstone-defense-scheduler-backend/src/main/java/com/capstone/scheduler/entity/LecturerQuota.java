package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecturer_quotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id")
    private Integer quotaId;


    // Chỉ tiêu của ai?
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Áp dụng cho đợt bảo vệ nào?
    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;


    @Column(name = "min_council")
    private Integer minCouncil = 0; // Tối thiểu phải ngồi 0

    @Column(name = "max_council")
    private Integer maxCouncil = 10; // Tối đa ngồi 7 hội đồng
}