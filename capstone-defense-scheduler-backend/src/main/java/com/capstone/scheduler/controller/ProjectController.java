package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.ProjectImportRequest;
import com.capstone.scheduler.dto.response.ProjectResponse;
import com.capstone.scheduler.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "APIs for managing Projects & Supervisors")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/import")
    @Operation(summary = "Import Projects List",
            description = "Import projects for a semester. Input lecturer names. The first name is MAIN supervisor, others are CO. (BR-02, BR-03)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import successful",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Lecturer name not found or invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Semester not found",
                    content = @Content)
    })
    public ResponseEntity<List<ProjectResponse>> importProjects(@RequestBody @Valid ProjectImportRequest request) {
        List<ProjectResponse> result = projectService.importProjects(request);
        return ResponseEntity.ok(result);
    }
}