package com.report.nexoreport.dto;

import java.util.List;

public record IssueDashboardResponse(
        long total,
        long pending,
        long inProgress,
        long resolved,
        List<IssueResponseDto> issues
) {}
