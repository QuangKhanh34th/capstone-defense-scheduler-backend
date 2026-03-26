package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveScheduleRequest {

    @NotNull(message = "Round ID is required")
    private Integer roundId;

    private List<AssignmentDto> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentDto {
        private Integer blockId;
        private Integer lecturerId;
        private Integer roleId;
    }
}
