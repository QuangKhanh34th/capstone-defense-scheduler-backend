package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.service.DefenseDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Defense Day Management", description = "APIs for managing dates within a defense round")
public class DefenseDayController {

    private final DefenseDayService defenseDayService;

    // GET LIST DEFENSE DAY
    @GetMapping("/{roundId}/days")
    @Operation(summary = "List Defense Days",
            description = "Get all defense dates for a specific round, ordered chronologically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Defense Round not found")
    })
    public ResponseEntity<List<DefenseDayResponse>> getDefenseDays(
            @Parameter(description = "ID of the Defense Round", required = true, example = "1")
            @PathVariable Integer roundId
    ) {
        List<DefenseDayResponse> response = defenseDayService.getAllDefenseDays(roundId);
        return ResponseEntity.ok(response);
    }
}