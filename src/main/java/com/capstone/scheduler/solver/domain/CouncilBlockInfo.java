package com.capstone.scheduler.solver.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable domain class representing council block information for the solver.
 * This is a problem fact, not a planning entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilBlockInfo {

    private Integer blockId;
    private String blockName;
    private LocalDate defenseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer roundId;

    // IDs of lecturers who supervise projects in this block (for conflict detection)
    @Builder.Default
    private Set<Integer> projectSupervisorIds = new HashSet<>();

    // IDs of projects assigned to this block
    @Builder.Default
    private Set<Integer> projectIds = new HashSet<>();

    /**
     * Check if this block overlaps with another block in time (same date, overlapping times)
     */
    public boolean overlapsWithBlock(CouncilBlockInfo other) {
        if (other == null || !this.defenseDate.equals(other.defenseDate)) {
            return false;
        }
        // Check time overlap
        return !this.endTime.isBefore(other.startTime) && !other.endTime.isBefore(this.startTime);
    }
}
