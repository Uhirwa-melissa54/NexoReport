package com.report.nexoreport.service;

import com.report.nexoreport.dto.NotificationResponseDto;
import com.report.nexoreport.exception.ResourceNotFoundException;
import com.report.nexoreport.notification.Notification;
import com.report.nexoreport.notification.NotificationType;
import com.report.nexoreport.repository.NotificationRepository;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.user.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUsers(Collection<User> users, String message, NotificationType type) {
        for (User user : users) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setType(type);
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    public List<NotificationResponseDto> myNotifications(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void markAsRead(Authentication authentication, Long notificationId) {
        User user = getCurrentUser(authentication);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponseDto toDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
