package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.SetCompetencyRequest;
import com.capstone.scheduler.service.CompetencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturers")
@RequiredArgsConstructor
@Tag(name = "Lecturer Competency Management", description = "APIs for managing lecturer weights/skills")
@PreAuthorize("hasRole('ADMIN')")
public class CompetencyController {

    private final CompetencyService competencyService;

    // Set Competency
    @PostMapping("/{lecturerId}/competencies")
    @Operation(summary = "Set Lecturer Competency Weight",
            description = "Set or update competency weight for a lecturer. " +
                    "<br><b>Upsert Logic:</b> Updates if exists, creates if new." +
                    "<br><b>Weight:</b> Represents the suitability for a specific role (Higher is better).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Competencies processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Weight (Must be >= 0)"),
            @ApiResponse(responseCode = "404", description = "Lecturer or Role not found")
    })
    public ResponseEntity<List<String>> setCompetency(
            @Parameter(description = "ID of the Lecturer", required = true, example = "1")
            @PathVariable Integer lecturerId,

            @RequestBody @Valid List<SetCompetencyRequest> requests
    ) {
        List<String> response = competencyService.setCompetencies(lecturerId, requests);
        return ResponseEntity.ok(response);
    }
}