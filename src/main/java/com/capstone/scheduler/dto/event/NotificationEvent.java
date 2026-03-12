package com.capstone.scheduler.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final Integer userId;
    private final String title;
    private final String body;
    private final Map<String, String> data;

    public NotificationEvent(Object source, Integer userId, String title, String body, Map<String, String> data) {
        super(source);
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.data = data;
    }
}
