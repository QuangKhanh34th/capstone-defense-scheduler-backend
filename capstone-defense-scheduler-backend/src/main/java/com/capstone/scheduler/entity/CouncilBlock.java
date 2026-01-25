package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "council_blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouncilBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Integer blockId;


    // Ca này thuộc về ngày nào
    @ManyToOne
    @JoinColumn(name = "day_id", nullable = false)
    private DefenseDay defenseDay;


    @Column(name = "block_name", length = 50, nullable = false)
    @NotBlank(message = "Tên ca là bắt buộc (VD: Ca Sáng)")
    private String blockName;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Giờ bắt đầu là bắt buộc")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "Giờ kết thúc là bắt buộc")
    private LocalTime endTime;

    // Số lượng nhóm dự kiến tối đa trong ca này (để thuật toán tính toán)
    @Column(name = "expected_project_count")
    private Integer expectedProjectCount;
}