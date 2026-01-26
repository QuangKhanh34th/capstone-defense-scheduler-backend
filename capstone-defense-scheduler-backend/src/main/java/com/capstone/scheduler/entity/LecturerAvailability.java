package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "lecturer_availabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LecturerAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Integer availabilityId;


    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;
}