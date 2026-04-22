package com.report.nexoreport.dto;

import java.util.List;

public record IssueThreadResponse(
        IssueResponseDto issue,
        List<CommentResponseDto> comments,
        List<IssueActivityResponseDto> activities
) {}
