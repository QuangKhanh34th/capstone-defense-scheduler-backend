package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "defense_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefenseDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_id")
    private Integer dayId;


    // Thuộc về đợt bảo vệ nào
    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;


    @Column(name = "defense_date", nullable = false)
    @NotNull(message = "Defense date is required")
    private LocalDate defenseDate;
}