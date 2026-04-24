package com.report.nexoreport.repository;

import com.report.nexoreport.notification.Notification;
import com.report.nexoreport.notification.NotificationType;
import com.report.nexoreport.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndTypeAndMessageAndCreatedAtAfter(User user, NotificationType type, String message, Instant createdAt);
}
