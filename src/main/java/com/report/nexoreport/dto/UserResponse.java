package com.report.nexoreport.dto;

import com.report.nexoreport.user.UserRole;
import com.report.nexoreport.user.UserStatus;
import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        String className,
        String committeePosition
) {
}
