package com.capstone.scheduler.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssignProjectsToBlockRequest {
    @NotEmpty(message = "Project IDs cannot be empty")
    private List<Integer> projectIds;
}