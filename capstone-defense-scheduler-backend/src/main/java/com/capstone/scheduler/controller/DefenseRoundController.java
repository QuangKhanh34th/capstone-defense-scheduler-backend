package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.DefenseRoundRequest;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.service.DefenseRoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Defense Round Management", description = "APIs for managing Defense Rounds")
public class DefenseRoundController {

    private final DefenseRoundService defenseRoundService;
    
    @PostMapping("/{semesterId}/rounds")
    @Operation(summary = "Create a new Defense Round",
            description = "Create a defense round container under a specific semester. " +
                    "Note: Specific dates will be managed in Defense Days.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Defense Round created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation Error (Missing Round Name)"),
            @ApiResponse(responseCode = "404", description = "Semester not found")
    })
    public ResponseEntity<DefenseRoundResponse> createRound(
            @Parameter(description = "ID of the Semester", required = true, example = "1")
            @PathVariable("semesterId") Integer semesterId,

            @RequestBody @Valid DefenseRoundRequest request
    ) {
        DefenseRoundResponse newRound = defenseRoundService.createRound(semesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRound);
    }
}