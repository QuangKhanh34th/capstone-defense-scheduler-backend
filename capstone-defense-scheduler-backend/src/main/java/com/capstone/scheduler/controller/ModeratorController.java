package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CompetencySetRequest;
import com.capstone.scheduler.dto.request.QuotaSetRequest;
import com.capstone.scheduler.entity.LecturerCompetency;
import com.capstone.scheduler.entity.LecturerQuota;
import com.capstone.scheduler.service.ModeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Moderator Management", description = "APIs for setting Quotas and Competencies (BR-21, BR-22, BR-26)")
public class ModeratorController {

    private final ModeratorService moderatorService;

    // --- 1. Set Lecturer Quota ---
    @PostMapping("/rounds/{roundId}/quotas")
    @Operation(summary = "Set Lecturer Quota per Round (BR-26)",
            description = "Set Min/Max Council assignments for lecturers in a specific round. Updates if exists, Creates if new.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quotas set successfully"),
            @ApiResponse(responseCode = "400", description = "Min > Max or Invalid Input"),
            @ApiResponse(responseCode = "404", description = "Round or Lecturer not found")
    })
    public ResponseEntity<List<LecturerQuota>> setLecturerQuotas(
            @PathVariable Integer roundId,
            @RequestBody @Valid QuotaSetRequest request) {

        List<LecturerQuota> result = moderatorService.setLecturerQuotas(roundId, request);
        return ResponseEntity.ok(result);
    }

    // --- 2. Set Competency ---
    @PostMapping("/lecturers/{lecturerId}/competencies")
    @Operation(summary = "Set Lecturer Competency (BR-21)",
            description = "Set competency score (0-5) for specific Council Roles. Used by algorithm to optimize assignments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Competencies set successfully"),
            @ApiResponse(responseCode = "400", description = "Score out of range (0-5) or Invalid Input"),
            @ApiResponse(responseCode = "404", description = "Lecturer or Role not found")
    })
    public ResponseEntity<List<LecturerCompetency>> setCompetencies(
            @PathVariable Integer lecturerId,
            @RequestBody @Valid CompetencySetRequest request) {

        List<LecturerCompetency> result = moderatorService.setLecturerCompetencies(lecturerId, request);
        return ResponseEntity.ok(result);
    }
}