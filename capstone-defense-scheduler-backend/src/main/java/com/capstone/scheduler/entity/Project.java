package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;


    @Column(name = "title", length = 255, nullable = false)
    @NotBlank(message = "Project title is required")
    private String title;

    @Column(name = "major", length = 100)
    private String major;

    // Trạng thái: APPROVED, REJECTED, DEFENDING
    @Column(name = "status", length = 20)
    private String status = "PENDING";
}