package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "council_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Integer blockId;

    // FOREIGN KEYS

    @NotNull(message = "Defense Day is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private DefenseDay defenseDay;

    // COLUMNS

    @Column(name = "block_name", length = 50, nullable = false)
    @NotBlank(message = "Block name is required")
    private String blockName;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Min(value = 0, message = "Project count cannot be negative")
    @Column(name = "expected_project_count")
    private Integer expectedProjectCount;

    // RELATIONSHIPS

    @OneToMany(mappedBy = "councilBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RoundBlock> roundBlocks;

    @OneToMany(mappedBy = "councilBlock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CouncilBlockAssignment> assignments;
}