package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.CouncilBlockResponse;
import com.capstone.scheduler.service.CouncilBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/days")
@RequiredArgsConstructor
@Tag(name = "Council Block Management", description = "APIs for creating defense sessions and assigning projects")
public class CouncilBlockController {

    private final CouncilBlockService councilBlockService;

    // CREATE COUNCIL BLOCK
    @PostMapping("/{dayId}/blocks")
    @Operation(summary = "Auto Create Blocks & Assign Projects",
            description = "Auto-distribute unassigned projects into new Blocks. " +
                    "<br><b>Flow:</b> Create CouncilBlock (Session) -> Create RoundBlock (Group) -> Assign Projects to RoundBlock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "No unassigned projects found"),
            @ApiResponse(responseCode = "404", description = "Defense Day not found")
    })
    public ResponseEntity<List<CouncilBlockResponse>> createCouncilBlocks(
            @Parameter(description = "ID of the Defense Day", required = true, example = "1")
            @PathVariable Integer dayId
    ) {
        List<CouncilBlockResponse> response = councilBlockService.autoCreateBlocksForDay(dayId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}