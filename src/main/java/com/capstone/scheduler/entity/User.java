package com.capstone.scheduler.entity;

import com.capstone.scheduler.enums.CommonStatus;
import com.capstone.scheduler.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    @NotBlank(message = "Password is required")
    private String passwordHash;

    // ADMIN, LECTURER
    @Column(name = "role", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.LECTURER;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    // RELATIONSHIPS

    // Kết nối 1-1 ngược lại với bảng Lecturer
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Lecturer lecturer;
}