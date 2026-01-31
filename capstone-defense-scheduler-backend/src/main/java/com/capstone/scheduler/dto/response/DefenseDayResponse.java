package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefenseDayResponse {
    private Integer dayId;
    private LocalDate defenseDate;
    private Integer roundId;
    private String roundName;
}