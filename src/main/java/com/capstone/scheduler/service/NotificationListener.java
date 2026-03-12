package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @Async("notificationTaskExecutor")
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event for user: {}", event.getUserId());
        notificationService.sendPushNotification(
                event.getUserId(),
                event.getTitle(),
                event.getBody(),
                event.getData()
        );
    }
}
