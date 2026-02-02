package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

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

    // Nhóm này thuộc về Block nào
    @NotNull(message = "Council Block is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private CouncilBlock councilBlock;

    // RELATIONSHIPS

    // Nhóm chứa nhiều Lượt bảo vệ của sinh viên
    @OneToMany(mappedBy = "roundBlock", fetch = FetchType.LAZY)
    private List<RoundProject> roundProjects;
}