package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecturer_compatibilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    // Giảng viên 1
    @ManyToOne
    @JoinColumn(name = "lecturer_1_id", nullable = false)
    private Lecturer lecturer1;

    // Giảng viên 2
    @ManyToOne
    @JoinColumn(name = "lecturer_2_id", nullable = false)
    private Lecturer lecturer2;


    // Điểm tương thích (VD: 0.0 đến 1.0)
    @Column(name = "compatibility_score")
    private Double compatibilityScore;
}