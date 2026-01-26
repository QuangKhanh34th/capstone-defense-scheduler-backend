package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.DefenseDayRequest;
import com.capstone.scheduler.entity.DefenseDay;
import com.capstone.scheduler.service.DefenseDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Defense Day Management", description = "Setup dates for a Defense Round")
public class DefenseDayController {

    private final DefenseDayService defenseDayService;

    @PostMapping("/{roundId}/days")
    @Operation(summary = "Add Defense Days",
            description = "Add specific dates to a Round (e.g., 2026-05-10). Returns Day IDs needed for creating Blocks.")
    public ResponseEntity<List<DefenseDay>> createDays(
            @PathVariable Integer roundId,
            @RequestBody @Valid DefenseDayRequest request) {

        List<DefenseDay> result = defenseDayService.createDays(roundId, request);
        return ResponseEntity.ok(result);
    }
}