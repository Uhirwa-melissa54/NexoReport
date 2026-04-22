package com.report.nexoreport.repository;

import com.report.nexoreport.notification.Notification;
import com.report.nexoreport.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
