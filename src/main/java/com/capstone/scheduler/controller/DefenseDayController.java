package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CreateDefenseDayRequest;
import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.service.DefenseDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Defense Day Management", description = "APIs for managing specific dates in a defense round")
@PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/{roundId}/days")
    @Operation(summary = "Create Defense Days",
            description = "Add multiple defense dates to a round at once. " +
                    "Transaction is All-or-Nothing: if one date fails validation (Sunday, Duplicate, Out of Range), none will be saved.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All Defense Days created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation Error (Sunday, Out of Semester Range, or Duplicate in Request)"),
            @ApiResponse(responseCode = "404", description = "Round not found"),
            @ApiResponse(responseCode = "409", description = "One of the dates already exists in DB")
    })
    public ResponseEntity<List<DefenseDayResponse>> createBulkDefenseDays(
            @Parameter(description = "ID of the Defense Round", required = true, example = "1")
            @PathVariable Integer roundId,

            @RequestBody @Valid CreateDefenseDayRequest request
    ) {
        List<DefenseDayResponse> response = defenseDayService.createDefenseDays(roundId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}