package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.service.ProjectImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
@Tag(name = "Project Management", description = "APIs for importing projects")
public class ProjectController {

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
}