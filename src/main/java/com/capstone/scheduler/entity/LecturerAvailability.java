package com.capstone.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "lecturer_availabilities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"lecturer_id", "round_id", "available_date"})
                // Ràng buộc QUAN TRỌNG:
                // Ngăn chặn việc lưu trùng lặp: Giảng viên A - Đợt 1 - Ngày 15/05 (xuất hiện 2 lần).
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Integer availabilityId;

    // FOREIGN KEYS

    @NotNull(message = "Lecturer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @NotNull(message = "Defense Round is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private DefenseRound defenseRound;

    // COLUMNS

    @NotNull(message = "Available date is required")
    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;
}