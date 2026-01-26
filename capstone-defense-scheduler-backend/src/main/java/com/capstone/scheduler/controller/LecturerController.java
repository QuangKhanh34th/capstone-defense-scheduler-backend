package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.LecturerImportRequest;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.service.LecturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturers")
@RequiredArgsConstructor
@Tag(name = "Lecturer Management", description = "APIs for managing lecturers and their accounts")
public class LecturerController {

    private final LecturerService lecturerService;

    @PostMapping("/import")
    @Operation(summary = "Import Lecturers & Create Accounts",
            description = "Import list of lecturers. Checks duplicates (Email, Code, Phone). Creates 'User' (passwordHash='123456', status='ACTIVE') and 'Lecturer' entities.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import successful"),
            @ApiResponse(responseCode = "400", description = "Validation error (Duplicate Email/Phone/Code)"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<List<Lecturer>> importLecturers(@RequestBody @Valid LecturerImportRequest request) {
        List<Lecturer> result = lecturerService.importLecturers(request);
        return ResponseEntity.ok(result);
    }
}