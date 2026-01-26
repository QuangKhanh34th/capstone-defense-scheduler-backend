package com.capstone.scheduler.solver.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable domain class representing lecturer information for the solver.
 * This is a problem fact used as a planning value.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerInfo {

    private Integer lecturerId;
    private String lecturerCode;
    private String fullName;
    private String email;
    private Integer departmentId;

    // Quota constraints for this round
    private Integer minCouncil;
    private Integer maxCouncil;

    // Dates when the lecturer is available
    @Builder.Default
    private Set<LocalDate> availableDates = new HashSet<>();

    // Projects supervised by this lecturer (IDs)
    @Builder.Default
    private Set<Integer> supervisedProjectIds = new HashSet<>();

    /**
     * Check if lecturer supervises a specific project
     */
    public boolean supervisesProject(Integer projectId) {
        return supervisedProjectIds.contains(projectId);
    }

    /**
     * Check if lecturer is available on a specific date
     */
    public boolean isAvailableOn(LocalDate date) {
        return availableDates.contains(date);
    }
}
