package com.report.nexoreport.dto;

public record PublicStatsResponse(
        long totalIssues,
        long resolvedIssues,
        double resolvedPercentage
) {
}
