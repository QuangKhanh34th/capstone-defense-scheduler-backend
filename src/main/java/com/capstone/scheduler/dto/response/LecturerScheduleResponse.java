package com.capstone.scheduler.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class LecturerScheduleResponse {
    private Integer assignmentId;
    
    // Block Info
    private Integer blockId;
    private String blockName;
    private LocalDate defenseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Lecturer Info
    private Integer lecturerId;
    private String lecturerCode;
    private String lecturerName;
    private String lecturerEmail;
    
    // Role Info
    private Integer roleId;
    private String roleCode;
    private String roleName;
    
}
