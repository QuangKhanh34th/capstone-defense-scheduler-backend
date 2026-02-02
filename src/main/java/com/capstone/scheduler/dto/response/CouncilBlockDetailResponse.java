package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class CouncilBlockDetailResponse {
    private Integer blockId;
    private String blockName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer currentProjectCount;

    private List<BlockProjectResponse> projects;
}