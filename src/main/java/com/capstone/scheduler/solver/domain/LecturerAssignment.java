package com.capstone.scheduler.solver.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.*;

/**
 * Timefold Planning Entity
 * Represents an assignment slot in a council block that needs a lecturer assigned.
 * Each council block needs 5 lecturers (one for each role).
 */
@PlanningEntity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LecturerAssignment {

    @PlanningId
    private Long id;

    // The council block this assignment belongs to
    private CouncilBlockInfo councilBlock;

    // The role to be filled (President, Secretary, Member, etc.)
    private CouncilRoleInfo role;

    // The lecturer assigned to this slot - this is the planning variable
    @PlanningVariable(valueRangeProviderRefs = "lecturerRange")
    private LecturerInfo lecturer;

    /**
     * Get the block ID for constraint matching
     */
    public Integer getBlockId() {
        return councilBlock != null ? councilBlock.getBlockId() : null;
    }

    /**
     * Get the lecturer ID for constraint matching
     */
    public Integer getLecturerId() {
        return lecturer != null ? lecturer.getLecturerId() : null;
    }

    /**
     * Check if the assigned lecturer supervises any project in this block
     */
    public boolean lecturerSupervisesProjectInBlock() {
        if (lecturer == null || councilBlock == null) {
            return false;
        }
        return councilBlock.getProjectSupervisorIds().contains(lecturer.getLecturerId());
    }

    /**
     * Check if the lecturer is available for this block
     */
    public boolean isLecturerAvailable() {
        if (lecturer == null || councilBlock == null) {
            return true; // No constraint if not assigned
        }
        return lecturer.getAvailableDates().contains(councilBlock.getDefenseDate());
    }

    @Override
    public String toString() {
        String blockName = councilBlock != null ? councilBlock.getBlockName() : "Unassigned Block";
        String roleName = role != null ? role.getRoleName() : "Unassigned Role";
        String lecturerName = lecturer != null ? lecturer.getFullName() + " (" + lecturer.getLecturerCode() + ")" : "Unassigned";
        
        return String.format("Assignment[%s | %s | %s]", blockName, roleName, lecturerName);
    }
}
