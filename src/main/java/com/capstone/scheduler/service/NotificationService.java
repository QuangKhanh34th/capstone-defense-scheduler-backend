package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.event.NotificationEvent;
import com.capstone.scheduler.dto.request.NotificationRequest;
import com.capstone.scheduler.dto.request.RegisterDeviceRequest;
import com.capstone.scheduler.enums.UserRole;
import com.capstone.scheduler.entity.DeviceToken;
import com.capstone.scheduler.entity.NotificationHistory;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.DeviceTokenRepository;
import com.capstone.scheduler.repository.NotificationRepository;
import com.capstone.scheduler.repository.UserRepository;
import com.capstone.scheduler.security.CustomUserDetailsService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendPushNotification(Integer userId, String title, String body, Map<String, String> data) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for notification: {}", userId);
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository.findAllByUser(user);
        if (tokens.isEmpty()) {
            log.info("No registered devices for user: {}", userId);
            return;
        }

        List<String> registrationTokens = tokens.stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .addAllTokens(registrationTokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Successfully sent {} messages. {} failures.", 
                     response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleFailures(response, tokens);
            }

            // Save to history
            NotificationHistory history = NotificationHistory.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .dataPayload(data != null ? data.toString() : null)
                    .status(NotificationHistory.NotificationStatus.SENT)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(history);

        } catch (FirebaseMessagingException e) {
            log.error("Firebase messaging error: {}. Cause: {}", e.getMessage(), 
                      (e.getCause() != null ? e.getCause().getMessage() : "No cause"));
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
    
            NotificationHistory history = NotificationHistory.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .status(NotificationHistory.NotificationStatus.FAILED)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(history);
        }
    }

    @Transactional
    public void sendPushToToken(String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .setToken(token)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Direct push sent successfully to token {}. ID: {}", token, response);
        } catch (FirebaseMessagingException e) {
            log.error("Direct push error: {} - {}", e.getMessagingErrorCode(), e.getMessage());
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("Token {} is UNREGISTERED. Suggesting immediate cleanup.", token);
                deviceTokenRepository.deleteByToken(token);
            }
        }
    }
    
    @Transactional
    public void broadcastToAllLecturers(String title, String body, Map<String, String> data) {
        log.info("Initiating broadcast notification to active lecturers: {}", title);
        
        // Lấy tất cả token của role LECTURER
        List<DeviceToken> lecturerTokens = deviceTokenRepository.findAllByUserRole(UserRole.LECTURER);
        
        // Lấy danh sách User duy nhất từ các Token này
        List<User> activeLecturers = lecturerTokens.stream()
                .map(DeviceToken::getUser)
                .distinct()
                .collect(Collectors.toList());

        if (activeLecturers.isEmpty()) {
            log.info("No active lecturers found with registered devices. Skipping broadcast.");
            return;
        }

        activeLecturers.forEach(user -> {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    user.getUserId(),
                    title,
                    body,
                    data
            ));
        });
        
        log.info("Broadcast events published for {} active lecturers.", activeLecturers.size());
    }

    @Transactional
    public String handleManualNotification(NotificationRequest request) {
        // Broadcast cho TOÀN BỘ GIẢNG VIÊN (Admin nhắc đăng ký lịch)
        broadcastToAllLecturers(
                request.getTitle(),
                request.getBody(),
                Map.of("type", "REGISTRATION_CALL")
        );
        return "Broadcast notification queued for all lecturers.";
    }

    @Transactional
    public String testPushToToken(String token) {
        log.info("Diagnostic: Testing push to specific token: {}", token);
        
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle("🔍 Diagnostic Test")
                        .setBody("Nếu bạn thấy tin này, Token của bạn hoàn toàn hợp lệ!")
                        .build())
                .addToken(token)
                .putData("diagnostic", "true")
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            if (response.getSuccessCount() > 0) {
                return "✅ SUCCESS: Token is valid and push was sent!";
            } else {
                FirebaseMessagingException ex = response.getResponses().get(0).getException();
                String error = ex.getMessagingErrorCode().name();
                String msg = ex.getMessage();
                log.error("Diagnostic failure: {} - {}", error, msg);
                
                if (error.equals("UNREGISTERED")) {
                    return "❌ FAILED (UNREGISTERED): Token exists but Firebase doesn't recognize it. " +
                           "Possible causes: Mismatched Project ID, mismatched Package Name (check 'Android.swd' case), " +
                           "or expired token.";
                }
                return "❌ FAILED: " + error + " - " + msg;
            }
        } catch (Exception e) {
            return "❌ ERROR: " + e.getMessage();
        }
    }

    @Transactional
    public void registerDevice(String username, RegisterDeviceRequest request) {
        log.info("Registering device token for user: {}. Token: {}, Platform: {}", 
                 username, request.getDeviceToken(), request.getPlatform());

        if (username == null || "anonymousUser".equals(username)) {
            throw new RuntimeException("User must be authenticated to register device token");
        }

        User currentUser = userDetailsService.loadUserEntityByUsername(username);

        // Xử lý token rotation: Nếu token đã tồn tại cho user khác, hãy cập nhật nó sang user hiện tại
        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getDeviceToken())
                .orElse(new DeviceToken());

        // Nếu token này đang thuộc về user khác, log lại để theo dõi
        if (deviceToken.getUser() != null && !deviceToken.getUser().getUserId().equals(currentUser.getUserId())) {
            log.info("Token was previously associated with user: {}. Moving to user: {}", 
                     deviceToken.getUser().getUsername(), username);
        }

        deviceToken.setToken(request.getDeviceToken());
        deviceToken.setPlatform(request.getPlatform());
        deviceToken.setUser(currentUser);
        deviceToken.setLastActive(LocalDateTime.now());

        deviceTokenRepository.save(deviceToken);
        log.info("Successfully registered/updated token for user: {}", username);
    }

    private void handleFailures(BatchResponse response, List<DeviceToken> tokens) {
        List<SendResponse> responses = response.getResponses();
        List<String> tokensToDelete = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                FirebaseMessagingException exception = responses.get(i).getException();
                MessagingErrorCode errorCode = exception.getMessagingErrorCode();
                String token = tokens.get(i).getToken();
                
                log.warn("Notification failure for token {}: {} - {}", 
                         token, errorCode, exception.getMessage());
                
                if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    log.info("Adding token to cleanup list: {}", token);
                    tokensToDelete.add(token);
                }
            }
        }

        if (!tokensToDelete.isEmpty()) {
            tokensToDelete.forEach(deviceTokenRepository::deleteByToken);
        }
    }
}
