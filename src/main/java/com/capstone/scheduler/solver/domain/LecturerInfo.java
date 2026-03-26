package com.capstone.scheduler.solver.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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

    // Map of Role ID to Competency Weight (e.g., weight for being President)
    @Builder.Default
    private Map<Integer, Double> roleCompetencyWeights = new HashMap<>();

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

    /**
     * Get competency weight for a specific role
     */
    public Double getRoleWeight(Integer roleId) {
        return roleCompetencyWeights.getOrDefault(roleId, 0.0);
    }

    @Override
    public String toString() {
        return String.format("Lecturer[%s - %s]", lecturerCode, fullName);
    }
}
