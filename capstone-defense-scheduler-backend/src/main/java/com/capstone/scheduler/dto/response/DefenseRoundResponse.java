package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefenseRoundResponse {
    private Integer roundId;
    private String roundName;
    private String description;

    private Integer semesterId;
    private String semesterName;

    // Trạng thái
    private String status;
}