package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "council_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_code", length = 20, nullable = false, unique = true)
    @NotBlank(message = "Role code is required")
    private String roleCode;

    @Column(name = "role_name", length = 100, nullable = false)
    @NotBlank(message = "Role name is required")
    private String roleName;
}