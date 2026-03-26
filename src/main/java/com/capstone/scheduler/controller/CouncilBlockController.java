package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.AssignProjectsToBlockRequest;
import com.capstone.scheduler.dto.response.BlockProjectResponse;
import com.capstone.scheduler.dto.response.CouncilBlockDetailResponse;
import com.capstone.scheduler.dto.response.CouncilBlockResponse;
import com.capstone.scheduler.service.CouncilBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/days")
@RequiredArgsConstructor
@Tag(name = "Council Block Management", description = "APIs for creating defense sessions and assigning projects")
@PreAuthorize("hasRole('ADMIN')")
public class CouncilBlockController {

    private final CouncilBlockService councilBlockService;

    // CREATE COUNCIL BLOCK
    @PostMapping("/{dayId}/blocks")
    @Operation(summary = "Auto Create Blocks & Assign Projects",
            description = "Creates a specific number of Council Blocks (Rooms) for a day and distributes unassigned projects evenly among them. " +
                    "If unassigned projects exceed room capacity, the remaining projects will be left for another day.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid block count or no projects found"),
            @ApiResponse(responseCode = "404", description = "Defense Day not found")
    })
    public ResponseEntity<List<CouncilBlockResponse>> createCouncilBlocks(
            @Parameter(description = "ID of the Defense Day", required = true, example = "1")
            @PathVariable Integer dayId,

            @Parameter(description = "Number of rooms available to create (e.g., 1 to 4)", required = true, example = "4")
            @RequestParam("numberOfBlocks") Integer numberOfBlocks
    ) {
        List<CouncilBlockResponse> response = councilBlockService.autoCreateBlocksForDay(dayId, numberOfBlocks);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET LIST OF COUNCIL BLOCKS
    @GetMapping("/{dayId}/blocks")
    @Operation(summary = "Get List of Council Blocks",
            description = "Retrieve all council blocks (sessions) for a specific day, including the list of projects assigned to each block.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Defense Day not found")
    })
    public ResponseEntity<List<CouncilBlockDetailResponse>> getBlocksByDay(
            @Parameter(description = "ID of the Defense Day", required = true, example = "10")
            @PathVariable Integer dayId
    ) {
        List<CouncilBlockDetailResponse> response = councilBlockService.getBlocksByDayId(dayId);
        return ResponseEntity.ok(response);
    }

    // Assign Projects to Block
    @PostMapping("/{blockId}/projects")
    @Operation(summary = "Assign Projects to Block (Manual)",
            description = "Manually add projects to a specific Council Block. " +
                    "<br><b>Logic:</b>" +
                    "<ul>" +
                    "<li>Validate Project belongs to the same Round.</li>" +
                    "<li>Check Max Capacity (7).</li>" +
                    "<li>If Project was in another block, it will be moved here.</li>" +
                    "<li><b>Auto-Recalculate EndTime</b> of the block based on new project count.</li>" +
                    "</ul>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Capacity exceeded or Invalid Round"),
            @ApiResponse(responseCode = "404", description = "Block or Project not found")
    })
    public ResponseEntity<List<BlockProjectResponse>> assignProjectsToBlock(
            @PathVariable Integer blockId,
            @RequestBody @Valid AssignProjectsToBlockRequest request
    ) {
        List<BlockProjectResponse> response = councilBlockService.assignProjectsToBlock(blockId, request);
        return ResponseEntity.ok(response);
    }

    // Get Projects in Block
    @GetMapping("/{blockId}/projects")
    @Operation(summary = "Get Projects in Block", description = "List all projects currently assigned to this block.")
    public ResponseEntity<List<BlockProjectResponse>> getProjectsInBlock(
            @PathVariable Integer blockId
    ) {
        List<BlockProjectResponse> response = councilBlockService.getProjectsInBlock(blockId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/days/{dayId}/blocks")
    @Operation(summary = "Delete all Blocks for a specific Day",
            description = "Deletes all Council Blocks created on a specific Defense Day. " +
                    "<b>System Logic:</b> Automatically removes any Lecturer assignments and " +
                    "releases all assigned Projects back to the unassigned pool.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blocks successfully deleted"),
            @ApiResponse(responseCode = "400", description = "No blocks found for this day"),
            @ApiResponse(responseCode = "404", description = "Defense Day not found")
    })
    public ResponseEntity<String> deleteAllBlocksForDay(
            @Parameter(description = "ID of the Defense Day", required = true)
            @PathVariable Integer dayId) {

        councilBlockService.deleteBlocksByDayId(dayId);
        return ResponseEntity.ok("All blocks for Defense Day ID " + dayId + " have been successfully deleted. Projects are released back to the pool.");
    }
}