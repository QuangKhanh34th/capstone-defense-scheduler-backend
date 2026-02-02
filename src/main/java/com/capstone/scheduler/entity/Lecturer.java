package com.capstone.scheduler.entity;

import com.capstone.scheduler.enums.CommonStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "lecturers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecturer_id")
    private Integer lecturerId;

    // FOREIGN KEYS

    // Liên kết 1-1 với bảng Users
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Liên kết N-1 với Department
    @NotNull(message = "Department is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // COLUMNS

    @Column(name = "lecturer_code", length = 20, nullable = false, unique = true)
    @NotBlank(message = "Lecturer code is required")
    private String lecturerCode;

    @Column(name = "full_name", length = 100, nullable = false)
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "phone", length = 20,nullable=false)
    @NotBlank(message = "Phone number is required")
    private String phone;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    // RELATIONSHIPS

    // Danh sách Đề tài đang hướng dẫn
    @OneToMany(mappedBy = "lecturer", fetch = FetchType.LAZY)
    private List<ProjectSupervisor> supervisedProjects;

    // Danh sách Các ca bảo vệ được phân công
    @OneToMany(mappedBy = "lecturer", fetch = FetchType.LAZY)
    private List<CouncilBlockAssignment> councilAssignments;

    // Danh sách Lịch rảnh
    @OneToMany(mappedBy = "lecturer", fetch = FetchType.LAZY)
    private List<LecturerAvailability> availabilities;

    // Danh sách Định mức Quota
    @OneToMany(mappedBy = "lecturer", fetch = FetchType.LAZY)
    private List<LecturerQuota> quotas;

    // Danh sách Năng lực chuyên môn
    @OneToMany(mappedBy = "lecturer", fetch = FetchType.LAZY)
    private List<LecturerCompetency> competencies;

    // Danh sách Tương thích
    @OneToMany(mappedBy = "lecturer1", fetch = FetchType.LAZY)
    private List<LecturerCompatibility> compatibilitiesAsLecturer1;

    @OneToMany(mappedBy = "lecturer2", fetch = FetchType.LAZY)
    private List<LecturerCompatibility> compatibilitiesAsLecturer2;
}