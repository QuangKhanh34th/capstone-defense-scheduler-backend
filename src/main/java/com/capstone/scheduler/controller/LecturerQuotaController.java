package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.SetQuotaRequest;
import com.capstone.scheduler.dto.response.ImportResultResponse;
import com.capstone.scheduler.service.LecturerQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Lecturer Quota Management", description = "APIs for setting min/max councils")
@PreAuthorize("hasRole('ADMIN')")
public class LecturerQuotaController {

    private final LecturerQuotaService lecturerQuotaService;

    // SET QUOTA FOR LECTURE
    @PostMapping("/{roundId}/quotas")
    @Operation(summary = "Set Lecturer Quotas",
            description = "Set Min/Max council participation for lecturers in a specific round. " +
                    "<br><b>Logic:</b>" +
                    "<ul>" +
                    "<li>Upsert: Create if new, Update if exists.</li>" +
                    "<li>Validation: Min <= Max.</li>" +
                    "</ul>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processed successfully"),
            @ApiResponse(responseCode = "400", description = "Min > Max or Invalid ID"),
            @ApiResponse(responseCode = "404", description = "Round or Lecturer not found")
    })
    public ResponseEntity<List<String>> setLecturerQuotas(
            @Parameter(description = "ID of the Defense Round", required = true, example = "1")
            @PathVariable Integer roundId,

            @RequestBody @Valid List<SetQuotaRequest> requests
    ) {
        List<String> response = lecturerQuotaService.setLecturerQuotas(roundId, requests);
        return ResponseEntity.ok(response);
    }


    // API 1: XUẤT FILE TEMPLATE QUOTA (DYNAMIC)
    @GetMapping(value = "/template-quota", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(
            summary = "Download Dynamic Quota Template",
            description = "Generates and downloads an Excel template pre-filled with all ACTIVE lecturers in the system. " +
                    "Users only need to input the 'Min Council' and 'Max Council' values and upload it back."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file downloaded successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error (Error generating Excel file)")
    })
    public ResponseEntity<ByteArrayResource> downloadDynamicQuotaTemplate() throws IOException {
        byte[] data = lecturerQuotaService.generateDynamicQuotaTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Quota_Template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new ByteArrayResource(data));
    }

    // API 2: IMPORT FILE QUOTA ĐÃ ĐIỀN VÀO HỆ THỐNG
    @PostMapping(value = "/{roundId}/quotas/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import Lecturer Quotas from Excel",
            description = "Upload the filled Quota Excel file to update Min/Max councils for lecturers in a specific Defense Round."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import process completed (Check response body for success/failure details)",
                    content = @Content(schema = @Schema(implementation = ImportResultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing file",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Defense Round not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error (File I/O error)",
                    content = @Content)
    })
    public ResponseEntity<ImportResultResponse> importQuotas(
            @Parameter(description = "ID of the Defense Round", required = true, example = "1")
            @PathVariable("roundId") Integer roundId,

            @Parameter(description = "The filled Quota Excel file (.xlsx)", required = true)
            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(lecturerQuotaService.importQuotasFromExcel(file, roundId));
    }
}