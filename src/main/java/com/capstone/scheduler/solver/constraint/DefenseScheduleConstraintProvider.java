package com.capstone.scheduler.solver.constraint;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.capstone.scheduler.solver.domain.LecturerAssignment;

/**
 * Defines all scheduling constraints for the defense scheduling problem.
 * 
 * Hard Constraints (MUST be satisfied):
 * - BR-25: No double booking - Lecturer cannot be in two overlapping blocks
 * - BR-31/32: Supervisor conflict - Lecturer cannot be in council for their supervised project
 * - BR-33: Availability - Lecturer must be available on the defense date
 * - BR-27: Quota - Lecturer cannot exceed max council assignments
 * - BR-17: Each block must have exactly 5 distinct roles filled
 * 
 * Soft Constraints (SHOULD be satisfied, for optimization):
 * - Prefer meeting minimum quota
 * - Prefer balanced workload distribution
 */
public class DefenseScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                lecturerMustBeAssigned(constraintFactory),
                noDoubleBooking(constraintFactory),
                supervisorConflict(constraintFactory),
                lecturerAvailability(constraintFactory),
                maxQuotaConstraint(constraintFactory),
                uniqueRolePerBlock(constraintFactory),
                uniqueLecturerPerBlock(constraintFactory),

                // Soft constraints
                minQuotaPreference(constraintFactory),
                balancedWorkload(constraintFactory),
                maximizeRoleCompetency(constraintFactory)
        };
    }

    // ==================== HARD CONSTRAINTS ====================

    /**
     * Every assignment slot must have a lecturer assigned
     */
    Constraint lecturerMustBeAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Lecturer must be assigned");
    }

    /**
     * BR-25: A lecturer cannot be in two council blocks that overlap in time
     */
    Constraint noDoubleBooking(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(LecturerAssignment.class,
                        // Same lecturer
                        Joiners.equal(LecturerAssignment::getLecturerId),
                        // Different blocks
                        Joiners.filtering((a1, a2) -> 
                                !a1.getBlockId().equals(a2.getBlockId()) &&
                                a1.getCouncilBlock().overlapsWithBlock(a2.getCouncilBlock())))
                .filter((a1, a2) -> a1.getLecturer() != null && a2.getLecturer() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("No double booking");
    }

    /**
     * BR-31/BR-32: A lecturer cannot be a council member for a block
     * that contains a project they supervise
     */
    Constraint supervisorConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null)
                .filter(LecturerAssignment::lecturerSupervisesProjectInBlock)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Supervisor conflict");
    }

    /**
     * BR-33: Lecturer must be marked "Available" for the specific slot to be assigned
     */
    Constraint lecturerAvailability(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null)
                .filter(assignment -> !assignment.isLecturerAvailable())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Lecturer availability");
    }

    /**
     * BR-27: Do not assign a lecturer more slots than their max_quota
     */
    Constraint maxQuotaConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null)
                .groupBy(LecturerAssignment::getLecturer, 
                        ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .filter((lecturer, count) -> count > lecturer.getMaxCouncil())
                .penalize(HardSoftScore.ONE_HARD, (lecturer, count) -> count - lecturer.getMaxCouncil())
                .asConstraint("Max quota exceeded");
    }

    /**
     * BR-17: Each council block must have exactly 5 distinct roles filled
     * Penalize if same role appears multiple times in same block
     */
    Constraint uniqueRolePerBlock(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(LecturerAssignment.class,
                        // Same block
                        Joiners.equal(LecturerAssignment::getBlockId),
                        // Same role
                        Joiners.equal(assignment -> assignment.getRole().getRoleId()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Unique role per block");
    }

    /**
     * A lecturer can only be assigned once per block
     */
    Constraint uniqueLecturerPerBlock(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(LecturerAssignment.class,
                        // Same block
                        Joiners.equal(LecturerAssignment::getBlockId),
                        // Same lecturer
                        Joiners.equal(LecturerAssignment::getLecturerId))
                .filter((a1, a2) -> a1.getLecturer() != null && a2.getLecturer() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Unique lecturer per block");
    }

    // ==================== SOFT CONSTRAINTS ====================

    /**
     * Prefer meeting minimum quota for lecturers
     */
    Constraint minQuotaPreference(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null)
                .groupBy(LecturerAssignment::getLecturer,
                        ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .filter((lecturer, count) -> count < lecturer.getMinCouncil())
                .penalize(HardSoftScore.ONE_SOFT, (lecturer, count) -> lecturer.getMinCouncil() - count)
                .asConstraint("Min quota preference");
    }

    /**
     * Prefer balanced workload - penalize lecturers with many more assignments than average
     */
    Constraint balancedWorkload(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null)
                .groupBy(LecturerAssignment::getLecturer,
                        ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                // Soft penalty for each assignment above 4 (encourages distribution)
                .filter((lecturer, count) -> count > 4)
                .penalize(HardSoftScore.ONE_SOFT, (lecturer, count) -> (count - 4) * (count - 4))
                .asConstraint("Balanced workload");
    }

    /**
     * Prefer assigning lecturers to roles where they have a high competency weight (e.g., President).
     */
    Constraint maximizeRoleCompetency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(LecturerAssignment.class)
                .filter(assignment -> assignment.getLecturer() != null && assignment.getRole() != null)
                // Reward the assignment based on the weight. Multiply by 10 to scale the double into an integer score.
                .reward(HardSoftScore.ONE_SOFT, 
                        assignment -> (int) Math.round(assignment.getLecturer().getRoleWeight(assignment.getRole().getRoleId()) * 10))
                .asConstraint("Maximize role competency");
    }
}
