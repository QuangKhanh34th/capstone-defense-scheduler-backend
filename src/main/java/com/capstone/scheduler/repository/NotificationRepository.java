package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.NotificationHistory;
import com.capstone.scheduler.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationHistory, Long> {
    Page<NotificationHistory> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
