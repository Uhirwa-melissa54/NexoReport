package com.report.nexoreport.dto;

import jakarta.validation.constraints.NotNull;

public class AssignIssueRequest {
    @NotNull
    private Long assignedUserId;

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}

