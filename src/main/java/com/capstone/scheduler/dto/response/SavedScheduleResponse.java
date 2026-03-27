package com.capstone.scheduler.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for returning a saved schedule from the database.
 * Omits solver-specific statistics and statuses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedScheduleResponse {

    private Integer roundId;
    private String roundName;

    // Statistics
    private Integer totalBlocks;
    private Integer totalAssignments;

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
}
