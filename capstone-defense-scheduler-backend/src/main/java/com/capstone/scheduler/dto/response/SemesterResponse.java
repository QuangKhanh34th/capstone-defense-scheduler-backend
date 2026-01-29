package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class SemesterResponse {
    private Integer semesterId;
    private String name;
    private String schoolYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}