package com.capstone.scheduler.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for a single lecturer assignment in a council block
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  LecturerAssignmentResponse {

    private Integer assignmentId;
    private Integer blockId;
    private String blockName;
    private LocalDate defenseDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer lecturerId;
    private String lecturerCode;
    private String lecturerName;
    private String lecturerEmail;

    private Integer roleId;
    private String roleCode;
    private String roleName;
}
