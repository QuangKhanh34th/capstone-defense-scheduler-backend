package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.SetQuotaRequest;
import com.capstone.scheduler.service.LecturerQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Lecturer Quota Management", description = "APIs for setting min/max councils")
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
}