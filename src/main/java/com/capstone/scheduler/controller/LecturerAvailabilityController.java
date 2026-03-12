package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.AvailabilityRequest;
import com.capstone.scheduler.dto.response.AvailabilityResponse;
import com.capstone.scheduler.dto.response.DefenseDayResponse;
import com.capstone.scheduler.dto.response.DefenseRoundResponse;
import com.capstone.scheduler.service.LecturerAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Lecturer Availability API
 * Provides endpoints for lecturers to register their available dates for defense rounds.
 */
@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Lecturer Availability", description = "APIs for managing lecturer availability for thesis defense")
@PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
public class LecturerAvailabilityController {

    private final LecturerAvailabilityService availabilityService;

    /**
     * Get all defense rounds
     */
    @GetMapping("/rounds")
    @Operation(
            summary = "Get all defense rounds",
            description = "Returns a list of all defense rounds available for registration"
    )
    public ResponseEntity<List<DefenseRoundResponse>> getAllRounds() {
        List<DefenseRoundResponse> rounds = availabilityService.getAllRounds();
        return ResponseEntity.ok(rounds);
    }

    /**
     * Get defense days for a specific round
     */
    @GetMapping("/rounds/{roundId}/days")
    @Operation(
            summary = "Get defense days by round",
            description = "Returns all defense days for a specific round, sorted by date"
    )
    public ResponseEntity<List<DefenseDayResponse>> getDaysByRound(@PathVariable Integer roundId) {
        List<DefenseDayResponse> days = availabilityService.getDaysByRound(roundId);
        return ResponseEntity.ok(days);
    }

    /**
     * Get lecturer's availability for a specific round
     */
    @GetMapping("/lecturer/{lecturerId}/round/{roundId}")
    @Operation(
            summary = "Get lecturer availability",
            description = "Returns all registered available dates for a lecturer in a specific round"
    )
    public ResponseEntity<List<AvailabilityResponse>> getLecturerAvailability(
            @PathVariable Integer lecturerId,
            @PathVariable Integer roundId) {
        List<AvailabilityResponse> availability = availabilityService.getAvailabilityByLecturerAndRound(lecturerId, roundId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Register availability
     */
    @PostMapping
    @Operation(
            summary = "Register availability",
            description = "Register a lecturer as available on a specific date for a defense round"
    )
    public ResponseEntity<AvailabilityResponse> registerAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.registerAvailability(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete availability by ID
     */
    @DeleteMapping("/{availabilityId}")
    @Operation(
            summary = "Delete availability",
            description = "Remove a specific availability registration"
    )
    public ResponseEntity<Void> deleteAvailability(@PathVariable Integer availabilityId) {
        availabilityService.deleteAvailability(availabilityId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete availability by lecturer, round, and date
     */
    @DeleteMapping("/lecturer/{lecturerId}/round/{roundId}/date/{date}")
    @Operation(
            summary = "Delete availability by date",
            description = "Remove availability for a specific lecturer on a specific date"
    )
    public ResponseEntity<Void> deleteByDate(
            @PathVariable Integer lecturerId,
            @PathVariable Integer roundId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        availabilityService.deleteByLecturerAndDate(lecturerId, roundId, date);
        return ResponseEntity.noContent().build();
    }
}
