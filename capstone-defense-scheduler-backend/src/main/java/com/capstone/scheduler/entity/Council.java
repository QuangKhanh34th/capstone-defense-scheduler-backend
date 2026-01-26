package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "councils")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Council {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "council_id")
    private Integer councilId;


    // Hội đồng này hoạt động trong Block (Ca) nào
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    private CouncilBlock councilBlock;
}