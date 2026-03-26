package com.capstone.scheduler.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for the scheduling result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingResponse {

    private Integer roundId;
    private String roundName;

    // Solver status
    private String solverStatus; // NOT_SOLVING, SOLVING_SCHEDULED, SOLVING_ACTIVE, SOLVING_ENDED
    private ScoreAnalysisDto scoreExplanation;
    private Integer hardScore;
    private Integer softScore;

    // Statistics
    private Integer totalBlocks;
    private Integer totalAssignments;
    private Integer assignedCount;
    private Integer unassignedCount;

    // The actual assignments
    @Builder.Default
    private List<LecturerAssignmentResponse> assignments = new ArrayList<>();

    // Grouped by block for easier viewing
    @Builder.Default
    private List<BlockAssignmentGroup> blockGroups = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlockAssignmentGroup {
        private Integer blockId;
        private String blockName;
        private String defenseDate;
        private String timeSlot;

        @Builder.Default
        private List<LecturerAssignmentResponse> assignments = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreAnalysisDto {
        private String totalScore;
        @Builder.Default
        private List<ConstraintMatchDto> constraints = new ArrayList<>();
        @Builder.Default
        private List<IndictmentDto> indictments = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConstraintMatchDto {
        private String constraintName;
        private int matchCount;
        private String scoreImpact;
        @Builder.Default
        private List<String> justifications = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IndictmentDto {
        private String assignedEntity;
        private String totalScoreImpact;
        private int matchCount;
    }
}
