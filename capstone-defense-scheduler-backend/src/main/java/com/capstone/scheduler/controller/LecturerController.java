package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CreateLecturerRequest;
import com.capstone.scheduler.dto.response.LecturerResponse;
import com.capstone.scheduler.service.LecturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lecturers")
@RequiredArgsConstructor
@Tag(name = "Lecturer Management", description = "APIs for managing lecturers")
public class LecturerController {

    private final LecturerService lecturerService;

    // GET LIST OF LECTURER
    @GetMapping
    @Operation(summary = "Get List of Lecturers",
            description = "Retrieve a paginated list of lecturers with search and filter capabilities. " +
                    "If 'roundId' is provided, the response will include Quota information for that specific round.")
    public ResponseEntity<Page<LecturerResponse>> getLecturers(
            @Parameter(description = "Search by lecturer name or code")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by Department ID")
            @RequestParam(required = false) Integer departmentId,

            @Parameter(description = "Defense Round ID (to retrieve Min/Max Quota)")
            @RequestParam(required = false) Integer roundId,

            @Parameter(description = "Page number (0-based index)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Size of the page")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sorting criteria (e.g., lecturerId,desc or fullName,asc)")
            @RequestParam(defaultValue = "lecturerId,asc") String[] sort
    ) {
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<LecturerResponse> result = lecturerService.getLecturers(keyword, departmentId, roundId, pageable);
        return ResponseEntity.ok(result);
    }

    //CREATE LECTURER
    @PostMapping
    @Operation(summary = "Create a new Lecturer (Manual)",
            description = "Manually create a lecturer account. This will automatically create a corresponding User account (default password '123456').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lecturer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (Missing fields, wrong email format, department not found)"),
            @ApiResponse(responseCode = "409", description = "Conflict: Email or Lecturer Code already exists"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<LecturerResponse> createLecturer(@RequestBody @Valid CreateLecturerRequest request) {
        LecturerResponse response = lecturerService.createLecturer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}