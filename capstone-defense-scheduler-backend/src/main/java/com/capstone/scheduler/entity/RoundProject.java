package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "round_projects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"round_id", "project_id"})
                // Ràng buộc: 1 Project chỉ được đăng ký 1 lần trong 1 Đợt
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_project_id")
    private Integer roundProjectId;

    // FOREIGN KEYS

    // Đề tài nào?
    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Thuộc đợt bảo vệ nào?
    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // Được xếp vào Nhóm/Phòng (RoundBlock) nào?
    // Nullable = true (lúc đầu chưa xếp lịch)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_block_id")
    private RoundBlock roundBlock;

    // COLUMNS

    // Trạng thái kết quả: PASSED, FAILED, IN_PROGRESS
    @Column(name = "result_status", length = 20)
    @Builder.Default
    private String resultStatus = "IN_PROGRESS";
}