package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "project_supervisors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "lecturer_id"})
                // Ràng buộc: 1 Giảng viên không được hướng dẫn CÙNG 1 đề tài 2 lần
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSupervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supervisor_id")
    private Integer supervisorId;

    // FOREIGN KEYS

    // Nối với Đề tài
    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Nối với Giảng viên
    @NotNull(message = "Lecturer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // COLUMNS

    // Vai trò: MAIN (Hướng dẫn chính), CO (Đồng hướng dẫn)
    @Column(name = "role_type", length = 20)
    @Builder.Default
    private String roleType = "MAIN";
}