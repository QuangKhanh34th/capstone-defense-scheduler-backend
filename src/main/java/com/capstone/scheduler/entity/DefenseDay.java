package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "defense_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefenseDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_id")
    private Integer dayId;

    // FOREIGN KEYS

    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // COLUMNS

    @NotNull(message = "Defense date is required")
    @Column(name = "defense_date", nullable = false)
    private LocalDate defenseDate;

    // RELATIONSHIPS

    // 1 Ngày có nhiều Ca bảo vệ
    @OneToMany(mappedBy = "defenseDay", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CouncilBlock> councilBlocks;
}