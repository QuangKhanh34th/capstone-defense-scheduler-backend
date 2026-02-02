package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlockProjectResponse {
    private Integer projectId;
    private String title;
    private String major;
    private String supervisorName;
}