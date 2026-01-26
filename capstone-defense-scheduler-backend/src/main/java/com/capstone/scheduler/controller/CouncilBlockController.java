package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.CouncilBlockRequest;
import com.capstone.scheduler.entity.CouncilBlock;
import com.capstone.scheduler.service.CouncilBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/days")
@RequiredArgsConstructor
@Tag(name = "Council Block Management", description = "Manage 90-minute time slots for defense")
public class CouncilBlockController {

    private final CouncilBlockService councilBlockService;

    @PostMapping("/{dayId}/blocks")
    @Operation(summary = "Create Council Block (Strict Mode)",
            description = "Create a 90-minute slot. Enforces 10-min break between slots. Max 7 slots/day. (BR-61, 62, 63)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Block created successfully"),
            @ApiResponse(responseCode = "400", description = "Violation of BR rules (Duration != 90m, No 10m break, Overlap)"),
            @ApiResponse(responseCode = "404", description = "Defense Day not found")
    })
    public ResponseEntity<CouncilBlock> createBlock(
            @PathVariable Integer dayId,
            @RequestBody @Valid CouncilBlockRequest request) {

        CouncilBlock createdBlock = councilBlockService.createBlock(dayId, request);
        return ResponseEntity.ok(createdBlock);
    }
}