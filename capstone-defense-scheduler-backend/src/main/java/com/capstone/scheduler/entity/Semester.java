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
    @NotBlank(message = "Tên học kỳ là bắt buộc (VD: Spring 2024)")
    private String name;

    @Column(name = "school_year", length = 20, nullable = false)
    @NotBlank(message = "Năm học là bắt buộc (VD: 2023-2024)")
    private String schoolYear;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "UPCOMING"; // ONGOING, CLOSED
}