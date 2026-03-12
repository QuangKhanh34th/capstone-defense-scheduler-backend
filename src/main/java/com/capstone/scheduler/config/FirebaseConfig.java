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

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                log.warn("Firebase service account file not found. Push notifications will not work.");
                return;
            }

            InputStream serviceAccount = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase Application has been initialized with Project ID: {}", app.getOptions().getProjectId());
                
                // Thử refresh token ngay lúc khởi động để bắt lỗi sớm
                try {
                    credentials.refreshAccessToken();
                    log.info("Firebase credentials verified: Access token refreshed successfully.");
                } catch (Exception te) {
                    log.error("CRITICAL: Firebase credentials verification failed! Error: {}", te.getMessage());
                    if (te.getCause() != null) {
                        log.error("Cause: {}", te.getCause().getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase: {}", e.getMessage());
        }
    }
}
