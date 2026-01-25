package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecturer_competencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Phù hợp với vai trò nào? (VD: role_id của chức Chủ tịch)
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private CouncilRole councilRole;


    // Điểm trọng số
    // Thuật toán sẽ ưu tiên chọn người có weight cao làm Chủ tịch
    @Column(name = "weight")
    private Double weight = 1.0;
}