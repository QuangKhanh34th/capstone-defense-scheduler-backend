package com.capstone.scheduler.dto.response;

import com.capstone.scheduler.enums.SemesterStatus;
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
    private SemesterStatus status;
}