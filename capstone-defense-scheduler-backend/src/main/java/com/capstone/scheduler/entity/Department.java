package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "Department name is required")
    private String name;

    @Column(name = "faculty_name", length = 100, nullable = false)
    @NotBlank(message = "Faculty name is required")
    private String facultyName;

    // RELATIONSHIPS

    // 1 Bộ môn có nhiều Giảng viên
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Lecturer> lecturers;
}