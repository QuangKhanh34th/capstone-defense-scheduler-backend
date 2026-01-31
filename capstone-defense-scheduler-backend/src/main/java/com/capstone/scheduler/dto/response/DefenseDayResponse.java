package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DefenseDayResponse {
    private Integer dayId;
    private LocalDate defenseDate;
    private Integer roundId;
    private String roundName;
}
