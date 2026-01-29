package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    // FOREIGN KEYS

    @NotNull(message = "Semester is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    // COLUMNS

    @NotBlank(message = "Project title is required")
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "status", length = 20)
    private String status;

    // --- RELATIONSHIPS

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<RoundProject> roundProjects;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ProjectSupervisor> projectSupervisors;
}