package com.capstone.scheduler.service;

import com.capstone.scheduler.entity.DeviceToken;
import com.capstone.scheduler.entity.NotificationHistory;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.DeviceTokenRepository;
import com.capstone.scheduler.repository.NotificationRepository;
import com.capstone.scheduler.repository.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .addToken(token)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Direct push sent to token {}. Success: {}, Failure: {}", 
                     token, response.getSuccessCount(), response.getFailureCount());
            
            if (response.getFailureCount() > 0) {
                FirebaseMessagingException exception = response.getResponses().get(0).getException();
                log.warn("Direct push failure: {} - {}", exception.getMessagingErrorCode(), exception.getMessage());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Direct push error: {}", e.getMessage());
        }
    }
    
    private void handleFailures(BatchResponse response, List<DeviceToken> tokens) {
        List<SendResponse> responses = response.getResponses();
        List<String> tokensToDelete = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                FirebaseMessagingException exception = responses.get(i).getException();
                MessagingErrorCode errorCode = exception.getMessagingErrorCode();
                log.warn("Notification failure for token {}: {} - {}", 
                         tokens.get(i).getToken(), errorCode, exception.getMessage());
                
                if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    log.info("Token {} is invalid/unregistered. Deleting from DB.", tokens.get(i).getToken());
                    tokensToDelete.add(tokens.get(i).getToken());
                }
            }
        }

        if (!tokensToDelete.isEmpty()) {
            tokensToDelete.forEach(deviceTokenRepository::deleteByToken);
        }
    }
}
