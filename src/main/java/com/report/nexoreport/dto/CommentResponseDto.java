package com.report.nexoreport.dto;

import java.time.Instant;

public record CommentResponseDto(
        Long id,
        Long issueId,
        String message,
        Long createdById,
        String createdByEmail,
        boolean privateToCreator,
        Instant createdAt
) {}
