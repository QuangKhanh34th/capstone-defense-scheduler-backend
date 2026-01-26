package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}