package com.report.nexoreport.dto;

import com.report.nexoreport.issue.IssueResponseType;
import java.time.Instant;

public record IssueActivityResponseDto(
        Long id,
        Long issueId,
        String message,
        Long respondedById,
        String respondedByEmail,
        IssueResponseType type,
        Instant createdAt
) {}
