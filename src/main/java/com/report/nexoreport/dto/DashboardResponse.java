package com.report.nexoreport.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalSubmitted,
        long totalReceived,
        long totalResolved,
        long totalInProgress,
        long totalPending,
        Map<String, Long> issuesByCategory,
        List<IssueResponseDto> recentSubmissions,
        List<IssueResponseDto> recentIssues
) {
}
