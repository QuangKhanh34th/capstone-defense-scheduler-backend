package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.AssignSupervisorRequest;
import com.capstone.scheduler.dto.response.ProjectSupervisorResponse;
import com.capstone.scheduler.service.SupervisorService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Supervisor Management", description = "APIs for assigning and viewing supervisors")
@PreAuthorize("hasRole('ADMIN')")
public class SupervisorController {

    private final SupervisorService supervisorService;

    // ADD SUPEVISTOR
    @PostMapping("/{projectId}/supervisors")
    @Operation(summary = "Assign Supervisor",
            description = "Assign a lecturer to a project using **Lecturer Code** (e.g., SE001). " +
                    "Rules: Lecturer must exist, must not be duplicated, max 1 MAIN supervisor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Role invalid or MAIN already exists"),
            @ApiResponse(responseCode = "404", description = "Project ID or Lecturer Code not found"),
            @ApiResponse(responseCode = "409", description = "Lecturer already assigned")
    })
    public ResponseEntity<ProjectSupervisorResponse> assignSupervisor(
            @Parameter(description = "ID of the Project", required = true, example = "1")
            @PathVariable Integer projectId,

            @RequestBody @Valid AssignSupervisorRequest request
    ) {
        ProjectSupervisorResponse response = supervisorService.assignSupervisor(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LIST OF SUPEVISTOR
    @GetMapping("/{projectId}/supervisors")
    @Operation(summary = "Get Supervisors of Project",
            description = "Retrieve the list of supervisors (MAIN and CO) for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<ProjectSupervisorResponse>> getSupervisors(
            @Parameter(description = "ID of the Project", required = true, example = "1")
            @PathVariable Integer projectId
    ) {
        List<ProjectSupervisorResponse> response = supervisorService.getSupervisors(projectId);
        return ResponseEntity.ok(response);
    }
}