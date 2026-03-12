package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "round_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_block_id")
    private Integer roundBlockId;

    // FOREIGN KEYS

    @NotNull(message = "Council Block is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private CouncilBlock councilBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_project_id")
    private RoundProject roundProject;

}