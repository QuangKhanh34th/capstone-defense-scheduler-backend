package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AvailabilityResponse {
    private Integer availabilityId;
    private Integer lecturerId;
    private String lecturerName;
    private Integer roundId;
    private String roundName;
    private LocalDate availableDate;
}
