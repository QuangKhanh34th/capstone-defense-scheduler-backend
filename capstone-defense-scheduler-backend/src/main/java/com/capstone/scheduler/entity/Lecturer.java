package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "lecturers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecturer_id")
    private Integer lecturerId;

    // 1. Liên kết 1-1 với bảng Users (Mỗi User chỉ là 1 Giảng viên)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 2. Liên kết N-1 với Department (Nhiều giảng viên thuộc 1 bộ môn)
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;


    @Column(name = "lecturer_code", length = 20, nullable = false, unique = true)
    @NotBlank(message = "Mã giảng viên là bắt buộc")
    private String lecturerCode;

    @Column(name = "full_name", length = 100, nullable = false)
    @NotBlank(message = "Họ tên là bắt buộc")
    private String fullName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @Column(name = "phone", length = 20)
    private String phone; // Có thể null

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Mặc định là đang hoạt động
}