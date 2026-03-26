package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.RegisterDeviceRequest;
import com.capstone.scheduler.dto.request.NotificationRequest;
import com.capstone.scheduler.dto.event.NotificationEvent;
import com.capstone.scheduler.entity.DeviceToken;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.DeviceTokenRepository;
import com.capstone.scheduler.security.CustomUserDetailsService;
import com.capstone.scheduler.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Management", description = "Endpoints for managing user device tokens for push notifications")
public class DeviceController {

    private final DeviceTokenRepository deviceTokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @PostMapping("/register")
    @Operation(summary = "Register or update a device token")
    public ResponseEntity<?> registerToken(@RequestBody RegisterDeviceRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            notificationService.registerDevice(username, request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Registration failed (Runtime): {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error registering token for user {}: {}", username, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/token/{token}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Unregister a device token")
    public ResponseEntity<Void> unregisterToken(@PathVariable String token) {
        deviceTokenRepository.deleteByToken(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notification")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send a manual notification to all lecturers or a specific user")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        try {
            String result = notificationService.handleManualNotification(request);
            return ResponseEntity.ok(result);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-token")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Diagnostic: Test a specific device token immediately")
    public ResponseEntity<String> testToken(@RequestParam String token) {
        String result = notificationService.testPushToToken(token);
        return ResponseEntity.ok(result);
    }
}
