package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.service.LecturerImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lecturers-import")
@RequiredArgsConstructor
@Tag(name = "Lecturer Import", description = "APIs for managing Lecturer Data Import via Excel")
@PreAuthorize("hasRole('ADMIN')")
public class LecturerImportController {

    private final LecturerImportService importService;

    // API DOWNLOAD TEMPLATE
    @GetMapping("/template")
    @Operation(
            summary = "Download Excel Template",
            description = "Download the standardized Excel template file (.xlsx) for importing lecturers. " +
                    "The file includes columns for User Info, Competencies, and Quotas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "500", description = "Template file not found on server",
                    content = @Content)
    })
    public ResponseEntity<InputStreamResource> downloadTemplate() throws IOException {
        InputStream in = importService.getExcelTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Lecturer_Import_Template.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import Lecturers from Excel",
            description = "Upload the filled Excel file to import lecturers (Master Data). " +
                    "The system will process row by row. If a row fails validation or Lecturer Code already exists, it will be SKIPPED. " +
                    "NOTE: Quotas are NOT set here. Please use the Quota Import API to set min/max councils for a specific round."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import process completed (check response body for detailed success/failure counts)",
                    content = @Content(schema = @Schema(implementation = ImportResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (Missing file...)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error (File I/O error)",
                    content = @Content)
    })
    public ResponseEntity<ImportResultResponse> importLecturers(
            @Parameter(description = "The Excel file (.xlsx) to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            ImportResultResponse result = importService.importLecturers(file);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ImportResultResponse.builder()
                    .successCount(0)
                    .failureCount(0)
                    .errorDetails(java.util.Collections.singletonList("Bad Request: " + e.getMessage()))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ImportResultResponse.builder()
                    .successCount(0)
                    .failureCount(0)
                    .errorDetails(java.util.Collections.singletonList("Internal Server Error: " + e.getMessage()))
                    .build());
        }
    }
}