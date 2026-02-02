package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImportResultResponse {
    private int successCount;
    private int failureCount;
    private List<String> errorDetails;
}