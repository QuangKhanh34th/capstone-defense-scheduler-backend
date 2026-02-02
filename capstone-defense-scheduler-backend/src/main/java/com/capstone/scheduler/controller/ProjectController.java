package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.dto.response.ProjectResponse;
import com.capstone.scheduler.service.ProjectImportService;
import com.capstone.scheduler.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectImportService projectImportService;

    //  API TẢI FILE MẪU
    @GetMapping("/import/template")
    @Operation(summary = "Download Import Template",
            description = "Download the standard Excel template for importing projects.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "500", description = "Template file not found on server",
                    content = @Content)
    })
    public ResponseEntity<Resource> downloadTemplate() throws IOException {

        InputStream inputStream = projectImportService.getExcelTemplate();
        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Project_Import_Template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import Projects (Excel)",
            description = "Format: [Col 1] Title | [Col 2] Major | [Col 3] Supervisor Code")
    public ResponseEntity<ImportResultResponse> importProjects(
            @RequestParam Integer roundId,
            @RequestPart("file") MultipartFile file
    ) {
        ImportResultResponse response = projectImportService.importProjects(file, roundId);
        return ResponseEntity.ok(response);
    }

    // LIST PROJECT
    @GetMapping
    @Operation(summary = "Get List of Projects",
            description = "Retrieve projects by Semester with optional filtering (search title, major) and pagination. " +
                    "Includes Main Supervisor information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Missing Semester ID")
    })
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @Parameter(description = "Semester ID (Required) - Scope of projects", required = true, example = "1")
            @RequestParam Integer semesterId,

            @Parameter(description = "Search by Project Title (Optional)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by Major (Optional) e.g., SE, AI")
            @RequestParam(required = false) String major,

            @Parameter(description = "Page number (0..N)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort (e.g., projectId,desc)")
            @RequestParam(defaultValue = "projectId,desc") String[] sort
    ) {
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort.length > 1 && sort[1].equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<ProjectResponse> result = projectService.getProjects(semesterId, search, major, pageable);
        return ResponseEntity.ok(result);
    }
}