package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class CouncilBlockResponse {
    private Integer blockId;
    private String blockName;
    private int projectCount;

    private LocalTime startTime;
    private LocalTime endTime;
}