package com.capstone.scheduler.solver.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Timefold Planning Solution for Defense Scheduling
 * This represents the entire scheduling problem for a defense round.
 */
@PlanningSolution
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefenseScheduleSolution {

    // Problem facts - immutable input data
    @ProblemFactCollectionProperty
    @Builder.Default
    private List<CouncilBlockInfo> councilBlocks = new ArrayList<>();

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "lecturerRange")
    @Builder.Default
    private List<LecturerInfo> lecturers = new ArrayList<>();

    @ProblemFactCollectionProperty
    @Builder.Default
    private List<CouncilRoleInfo> roles = new ArrayList<>();

    // Planning entities - to be assigned by the solver
    @PlanningEntityCollectionProperty
    @Builder.Default
    private List<LecturerAssignment> assignments = new ArrayList<>();

    // Score - calculated by constraint provider
    @PlanningScore
    private HardSoftScore score;

    // Metadata
    private Integer roundId;
    private String roundName;
}
