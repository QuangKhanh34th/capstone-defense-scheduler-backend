package com.capstone.scheduler.entity;

import com.capstone.scheduler.enums.RoundProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "round_projects",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"round_id", "project_id"})
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

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // COLUMNS

    @Column(name = "result_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoundProjectStatus resultStatus = RoundProjectStatus.IN_PROGRESS;

    // RELATIONSHIPS

    @OneToMany(mappedBy = "roundProject", fetch = FetchType.LAZY)
    private List<RoundBlock> roundBlocks;
}