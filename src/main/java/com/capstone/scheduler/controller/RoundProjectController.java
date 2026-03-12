package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.AddProjectToRoundRequest;
import com.capstone.scheduler.dto.response.RoundProjectResponse;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.service.RoundProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds/{roundId}/projects")
@RequiredArgsConstructor
@Tag(name = "Round-Project Management", description = "APIs for managing projects within a defense round")
@PreAuthorize("hasRole('ADMIN')")
public class RoundProjectController {

    private final RoundProjectService roundProjectService;

    // Add Projects to Round
    @PostMapping
    @Operation(summary = "Add Projects to Round",
            description = "Add a list of existing Projects into a Defense Round. " +
                    "<br><b>Rules:</b>" +
                    "<ul>" +
                    "<li>Project must belong to the same Semester as the Round. (Fail if not match)</li>" +
                    "<li>If Project is already in the Round, it will be skipped (Idempotent).</li>" +
                    "</ul>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processed. Returns list of actions (Added/Skipped)."),
            @ApiResponse(responseCode = "400", description = "Validation Error (Wrong Semester)"),
            @ApiResponse(responseCode = "404", description = "Round or Projects not found")
    })
    public ResponseEntity<List<String>> addProjectsToRound(
            @Parameter(description = "ID of the Defense Round", required = true, example = "1")
            @PathVariable Integer roundId,

            @RequestBody @Valid AddProjectToRoundRequest request
    ) {
        List<String> result = roundProjectService.addProjectsToRound(roundId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get list of projects in a round",
            description = "Fetch round projects with dynamic search, filter, sorting, and pagination. " +
                    "Sorting example: sort=project.title,asc")
    public ResponseEntity<Page<RoundProjectResponse>> getRoundProjects(
            @PathVariable Integer roundId,

            @Parameter(description = "Search keyword for project title")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by Project Status (e.g., PENDING, COMPLETED)")
            @RequestParam(required = false) ProjectStatus status,

            @Parameter(description = "Filter by Major")
            @RequestParam(required = false) String major,

            @Parameter(description = "Pagination and Sorting. Default is page 0, size 20.")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RoundProjectResponse> response = roundProjectService.getRoundProjects(roundId, search, status, major, pageable);
        return ResponseEntity.ok(response);
    }
}