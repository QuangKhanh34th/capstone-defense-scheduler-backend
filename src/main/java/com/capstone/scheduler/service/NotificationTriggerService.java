package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.event.NotificationEvent;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTriggerService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public void notifyScheduleReleased(Integer userId, String roundName) {
        String title = "Lịch bảo vệ mới!";
        String body = String.format("Lịch bảo vệ cho đợt '%s' đã được cập nhật. Hãy kiểm tra ngay!", roundName);
        
        Map<String, String> data = Map.of(
                "type", "SCHEDULE_RELEASED",
                "roundName", roundName
        );

        eventPublisher.publishEvent(new NotificationEvent(this, userId, title, body, data));
    }

    public void notifyNewRoundCreated(Integer roundId, String roundName) {
        String title = "📢 Đợt bảo vệ mới: " + roundName;
        String body = "Hệ thống đã mở đợt bảo vệ mới. Vui lòng đăng ký lịch rảnh của bạn ngay!";
        
        Map<String, String> data = Map.of(
                "type", "NEW_ROUND",
                "roundId", roundId.toString(),
                "roundName", roundName
        );

        notifyAllLecturers(title, body, data);
    }

    public void notifyAllLecturers(String title, String body, Map<String, String> data) {
        userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equals("LECTURER"))
                .forEach(user -> eventPublisher.publishEvent(
                        new NotificationEvent(this, user.getUserId(), title, body, data)
                ));
    }
}
