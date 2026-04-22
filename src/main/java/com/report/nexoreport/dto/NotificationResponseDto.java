package com.report.nexoreport.dto;

import com.report.nexoreport.notification.NotificationType;
import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        String message,
        NotificationType type,
        boolean isRead,
        Instant createdAt
) {}
