package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "council_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    //  RELATIONSHIPS

    // Danh sách các phân công sử dụng vai trò này
    // Giúp kiểm tra ràng buộc: Không được xóa role nếu đang có người giữ chức vụ này
    @OneToMany(mappedBy = "councilRole", fetch = FetchType.LAZY)
    private List<CouncilBlockAssignment> assignments;
}