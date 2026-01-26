package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.AvailabilityRegisterRequest;
import com.capstone.scheduler.entity.LecturerAvailability;
import com.capstone.scheduler.service.AvailabilityService;
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
@RequestMapping("/api/v1/lecturer/availability")
@RequiredArgsConstructor
@Tag(name = "Lecturer Availability", description = "Register lecturer availability for defense rounds")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    @Operation(summary = "Register Availability (By Date)",
            description = "Lecturer selects dates they are free. IMPORTANT: The selected dates MUST exist in the 'DefenseDay' list of the chosen Round.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date (Not in DefenseDay list) or Missing inputs"),
            @ApiResponse(responseCode = "404", description = "Lecturer or Round not found")
    })
    public ResponseEntity<List<LecturerAvailability>> registerAvailability(
            @RequestBody @Valid AvailabilityRegisterRequest request) {

        List<LecturerAvailability> result = availabilityService.registerAvailability(request);
        return ResponseEntity.ok(result);
    }
}