package com.capstone.scheduler.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @org.springframework.beans.factory.annotation.Value("${FIREBASE_CONFIG_JSON:}")
    private String firebaseConfigJson;

    @PostConstruct
    public void initialize() {
        try {
            GoogleCredentials credentials;

            // 1. Ưu tiên đọc từ ENV (Bảo mật cho Git)
            if (firebaseConfigJson != null && !firebaseConfigJson.isBlank()) {
                log.info("Loading Firebase credentials from environmental variable (FIREBASE_CONFIG_JSON)...");
                credentials = GoogleCredentials.fromStream(
                        new java.io.ByteArrayInputStream(firebaseConfigJson.getBytes())
                );
            } 
            // 2. Fallback về file local
            else {
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (!resource.exists()) {
                    log.warn("Firebase config not found. Push notifications disabled.");
                    return;
                }
                log.info("Loading Firebase credentials from: firebase-service-account.json");
                credentials = GoogleCredentials.fromStream(resource.getInputStream());
            }

            // Lấy Project ID TRƯỚC khi gọi createScoped
            String projectId = null;
            if (credentials instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
                projectId = ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getProjectId();
            }

            // Fix lỗi invalid_scope: Cung cấp đầy đủ các scope cần thiết
            credentials = credentials.createScoped(java.util.Arrays.asList(
                    "https://www.googleapis.com/auth/firebase.messaging",
                    "https://www.googleapis.com/auth/cloud-platform"
            ));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase Application initialized for Project: {}", app.getOptions().getProjectId());
                
                // Verify Token (Check mạng & Auth)
                try {
                    credentials.refreshAccessToken();
                    log.info("Firebase verification: Token refreshed successfully.");
                } catch (Exception te) {
                    log.error("Firebase Verification Failed: {}", te.getMessage());
                    if (te.getMessage().contains("400 Bad Request") && te.getMessage().contains("invalid_scope")) {
                        log.warn("TIP: If scope error persists, ensure Service Account has 'Firebase Messaging Admin' role.");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase: {}", e.getMessage());
        }
    }
}
