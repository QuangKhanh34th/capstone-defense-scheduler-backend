package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CreateSemesterRequest;
import com.capstone.scheduler.dto.response.SemesterResponse;
import com.capstone.scheduler.service.SemesterService;
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
@RequestMapping("/api/v1/semesters")
@RequiredArgsConstructor
@Tag(name = "Semester Management", description = "APIs for managing academic semesters")
public class SemesterController {

    private final SemesterService semesterService;

    // GET LIST
    @GetMapping
    @Operation(summary = "Get List of Semesters",
            description = "Retrieve a paginated list of semesters. Supports searching by name/year and filtering by status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Invalid sort parameter or pagination")
    })
    public ResponseEntity<Page<SemesterResponse>> getSemesters(
            @Parameter(description = "Search by semester name or school year (e.g., 'Spring', '2026')")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by status (e.g., UPCOMING, ONGOING, FINISHED)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Page number (0-based index)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Size of the page")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sorting criteria (e.g., startDate,desc or name,asc)")
            @RequestParam(defaultValue = "startDate,desc") String[] sort
    ) {
        // Xử lý Sort (Mặc định sort theo startDate giảm dần để thấy kỳ mới nhất)
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        Page<SemesterResponse> result = semesterService.getSemesters(keyword, status, pageable);
        return ResponseEntity.ok(result);
    }
}