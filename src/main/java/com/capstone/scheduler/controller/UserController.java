package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.response.UserResponse;
import com.capstone.scheduler.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and management endpoints")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Get User Profile", description = "Get profile information of the currently logged-in user")
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Attempt to access user profile without authentication principal.");
            return ResponseEntity.status(401).build();
        }
        try {
            UserResponse response = userService.getUserProfile(userDetails.getUsername());
            log.info("Successfully retrieved profile for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving profile for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
