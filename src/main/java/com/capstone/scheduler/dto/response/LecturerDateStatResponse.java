package com.capstone.scheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDateStatResponse {

    private LocalDate date;
    private Long lecturerCount;
    private Boolean isLowTurnout;
    private String statusMessage;
}