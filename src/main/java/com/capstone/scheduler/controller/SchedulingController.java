package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.SaveScheduleRequest;
import com.capstone.scheduler.dto.request.SchedulingRequest;
import com.capstone.scheduler.dto.response.SavedScheduleResponse;
import com.capstone.scheduler.dto.response.SchedulingResponse;
import com.capstone.scheduler.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Lecturer Scheduling API
 * Provides endpoints for scheduling lecturers to thesis defense councils
 * using Timefold Solver optimization.
 */
@RestController
@RequestMapping("/api/v1/scheduling")
@RequiredArgsConstructor
@Tag(name = "Scheduling", description = "APIs for scheduling lecturers to thesis defense councils")
@PreAuthorize("hasRole('ADMIN')")
public class SchedulingController {

    private final SchedulingService schedulingService;

    /**
     * Start the scheduling optimization for a defense round
     */
    @PostMapping("/solve")
    @Operation(
            summary = "Start scheduling optimization",
            description = "Initiates the Timefold Solver to assign lecturers to council blocks for a specific defense round. " +
                    "The solver will optimize assignments based on constraints like availability, quotas, and supervisor conflicts."
    )
    public ResponseEntity<SchedulingResponse> startScheduling(
            @Valid @RequestBody SchedulingRequest request) {
        SchedulingResponse response = schedulingService.startScheduling(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current status of a scheduling run
     */
    @GetMapping("/status/{roundId}")
    @Operation(
            summary = "Get scheduling status",
            description = "Returns the current status of the scheduling solver for a specific defense round. Available status: SOLVING_SCHEDULED, SOLVING_ACTIVE, NOT_SOLVING"
    )
    public ResponseEntity<SchedulingResponse> getSchedulingStatus(
            @PathVariable Integer roundId) {
        SchedulingResponse response = schedulingService.getSchedulingStatus(roundId);
        return ResponseEntity.ok(response);
    }

    /**
     * Stop an ongoing scheduling process
     */
    @PostMapping("/stop/{roundId}")
    @Operation(
            summary = "Stop scheduling",
            description = "Terminates the scheduling solver early for a specific defense round."
    )
    public ResponseEntity<Void> stopScheduling(
            @PathVariable Integer roundId) {
        schedulingService.stopScheduling(roundId);
        return ResponseEntity.ok().build();
    }

    /**
     * Save the scheduling result to database
     */
    @PostMapping("/save")
    @Operation(
            summary = "Save scheduling result",
            description = "Saves a provided (and potentially user-modified) schedule to the database. This will overwrite any existing schedule for the round specified in the request body."
    )
    public ResponseEntity<Void> saveSchedulingResult(
            @Valid @RequestBody SaveScheduleRequest scheduleToSave) {
        schedulingService.saveSchedulingResult(scheduleToSave);
        return ResponseEntity.ok().build();
    }

    /**
     * Get the saved schedule for a specific defense round
     */
    @GetMapping("/{roundId}")
    @Operation(
            summary = "Get saved schedule",
            description = "Returns the saved schedule (lecturer assignments) for a specific defense round."
    )
    public ResponseEntity<SavedScheduleResponse> getSavedSchedule(
            @PathVariable Integer roundId) {
        SavedScheduleResponse response = schedulingService.getSavedSchedule(roundId);
        return ResponseEntity.ok(response);
    }
}
