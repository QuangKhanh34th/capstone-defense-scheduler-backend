package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.SchedulingRequest;
import com.capstone.scheduler.dto.response.SchedulingResponse;
import com.capstone.scheduler.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            description = "Returns the current status of the scheduling solver for a specific defense round."
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
    @PostMapping("/save/{roundId}")
    @Operation(
            summary = "Save scheduling result",
            description = "Runs the solver and saves the optimized lecturer assignments to the database."
    )
    public ResponseEntity<SchedulingResponse> saveSchedulingResult(
            @PathVariable Integer roundId) {
        SchedulingResponse response = schedulingService.saveSchedulingResult(roundId);
        return ResponseEntity.ok(response);
    }
}
