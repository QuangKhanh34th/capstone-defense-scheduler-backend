package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_supervisors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSupervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supervisor_id")
    private Integer supervisorId;


    // 1. Nối với Đề tài
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // 2. Nối với Giảng viên
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;


    // Vai trò: MAIN (Hướng dẫn chính), CO (Đồng hướng dẫn)
    @Column(name = "role_type", length = 20)
    private String roleType = "MAIN";
}