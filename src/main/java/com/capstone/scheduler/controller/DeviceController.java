package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.request.RegisterDeviceRequest;
import com.capstone.scheduler.dto.request.TestNotificationRequest;
import com.capstone.scheduler.dto.event.NotificationEvent;
import com.capstone.scheduler.entity.DeviceToken;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.DeviceTokenRepository;
import com.capstone.scheduler.security.CustomUserDetailsService;
import com.capstone.scheduler.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Endpoints for managing user device tokens for push notifications")
public class DeviceController {

    private final DeviceTokenRepository deviceTokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @PostMapping("/register")
    // @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Register or update a device token")
    public ResponseEntity<Void> registerToken(@RequestBody RegisterDeviceRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userDetailsService.loadUserEntityByUsername(username);

        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getDeviceToken())
                .orElse(new DeviceToken());

        deviceToken.setToken(request.getDeviceToken());
        deviceToken.setPlatform(request.getPlatform());
        deviceToken.setUser(currentUser); // Transfer ownership if token already exists for another user
        deviceToken.setLastActive(LocalDateTime.now());

        deviceTokenRepository.save(deviceToken);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token/{token}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Unregister a device token")
    public ResponseEntity<Void> unregisterToken(@PathVariable String token) {
        deviceTokenRepository.deleteByToken(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test-push")
    @Operation(summary = "Send a test push notification (to username or direct deviceToken)")
    public ResponseEntity<String> testPush(@RequestBody TestNotificationRequest request) {
        // Trường hợp 1: Nhập token trực tiếp
        if (request.getDeviceToken() != null && !request.getDeviceToken().isBlank()) {
            notificationService.sendPushToToken(
                    request.getDeviceToken(), 
                    request.getTitle(), 
                    request.getBody(), 
                    java.util.Map.of("type", "TEST_DIRECT")
            );
            return ResponseEntity.ok("Direct test notification sent to token: " + request.getDeviceToken());
        }

        // Trường hợp 2: Gửi theo username
        String targetUsername = request.getUsername();
        
        // Nếu không truyền username trong body, thử lấy từ SecurityContext (nếu có login)
        if (targetUsername == null || targetUsername.isBlank()) {
            targetUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        if (targetUsername == null || "anonymousUser".equals(targetUsername)) {
            return ResponseEntity.badRequest().body("Vui lòng cung cấp username trong request body để test!");
        }

        try {
            User currentUser = userDetailsService.loadUserEntityByUsername(targetUsername);

            eventPublisher.publishEvent(new NotificationEvent(
                    this, 
                    currentUser.getUserId(), 
                    request.getTitle(), 
                    request.getBody(), 
                    java.util.Map.of("type", "TEST_PUSH")
            ));

            return ResponseEntity.ok("Test notification queued for user: " + targetUsername);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(404).body("Không tìm thấy user: " + targetUsername);
        }
    }
}
