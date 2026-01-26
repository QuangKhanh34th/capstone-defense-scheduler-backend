package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Integer semesterId;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "Semester name is required (e.g., Spring 2024)")
    private String name;

    @Column(name = "school_year", length = 20, nullable = false)
    @NotBlank(message = "School year is required (e.g., 2023-2024)")
    private String schoolYear;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "UPCOMING"; // ONGOING, CLOSED
}