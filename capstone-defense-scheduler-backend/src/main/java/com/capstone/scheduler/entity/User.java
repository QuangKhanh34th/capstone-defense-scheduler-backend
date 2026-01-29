package com.capstone.scheduler.entity;

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

    // ADMIN, LECTURER, STUDENT...
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private String role = "LECTURER";

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    // RELATIONSHIPS

    // Kết nối 1-1 ngược lại với bảng Lecturer
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Lecturer lecturer;
}