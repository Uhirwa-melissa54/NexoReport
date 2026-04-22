package com.report.nexoreport.dto;

import com.report.nexoreport.issue.IssuePriority;
import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.user.UserRole;
import java.time.Instant;

public record IssueResponseDto(
        Long id,
        String title,
        String description,
        String category,
        IssuePriority priority,
        IssueStatus status,
        Instant createdAt,
        Long createdById,
        String createdByEmail,
        IssueTargetType targetType,
        Long targetUserId,
        UserRole targetRole
) {}
