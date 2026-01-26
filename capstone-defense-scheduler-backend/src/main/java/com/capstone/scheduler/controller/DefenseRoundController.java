package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.DefenseRoundRequest;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.service.DefenseRoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Defense Round", description = "APIs for managing Defense Rounds")
public class DefenseRoundController {

    private final DefenseRoundService defenseRoundService;

    @PostMapping
    @Operation(summary = "Create a new Defense Round", description = "Create a defense round for a specific semester based on BR-04 and BR-05.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Defense Round created successfully",
                    content = @Content(schema = @Schema(implementation = DefenseRoundResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Missing round name, null ID...)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Semester not found",
                    content = @Content)
    })
    public ResponseEntity<DefenseRoundResponse> createRound(@RequestBody @Valid DefenseRoundRequest request) {
        com.capstone.scheduler.dto.response.DefenseRoundResponse newRound = defenseRoundService.createRound(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRound);
    }

    @GetMapping
    @Operation(summary = "Get all Defense Rounds", description = "Retrieve a list of all defense rounds.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of defense rounds retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DefenseRoundResponse.class)))
    })
    public ResponseEntity<List<DefenseRoundResponse>> getAllRounds() {
        List<DefenseRoundResponse> rounds = defenseRoundService.getAllRounds();
        return ResponseEntity.ok(rounds);
    }
}