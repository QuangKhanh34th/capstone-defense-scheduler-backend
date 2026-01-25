package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "round_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_project_id")
    private Integer roundProjectId;


    // 1. Đề tài nào?
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // 2. Thuộc đợt bảo vệ nào?
    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // 3. Được xếp vào Hội đồng nào?
    // Lưu ý: nullable = true vì lúc mới đăng ký chưa có hội đồng
    @ManyToOne
    @JoinColumn(name = "council_id", nullable = true)
    private Council council;


    // Trạng thái kết quả: PASSED, FAILED, IN_PROGRESS
    @Column(name = "result_status", length = 20)
    private String resultStatus = "IN_PROGRESS";
}