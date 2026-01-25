package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 50, nullable = false)
    @NotBlank(message = "Username là bắt buộc")
    private String username;

    @Column(name = "password_hash", length = 255, nullable = false)
    @NotBlank(message = "Password là bắt buộc")
    private String passwordHash;

    @Column(name = "role", length = 20, nullable = false)
    private String role = "LECTURE";

    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";
}