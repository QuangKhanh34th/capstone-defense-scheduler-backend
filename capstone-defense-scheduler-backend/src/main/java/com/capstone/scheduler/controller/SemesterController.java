package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CreateSemesterRequest;
import com.capstone.scheduler.dto.response.SemesterResponse;
import com.capstone.scheduler.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
@Tag(name = "Semester Management", description = "APIs for managing academic semesters")
public class SemesterController {

    private final SemesterService semesterService;

    @PostMapping
    @Operation(summary = "Create a new Semester",
            description = "Create a new academic semester. Validates that the End Date is after the Start Date and checks for unique semester name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Semester created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (Validation error) or End Date is before Start Date"),
            @ApiResponse(responseCode = "409", description = "Semester name already exists (Conflict)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<SemesterResponse> createSemester(@RequestBody @Valid CreateSemesterRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}