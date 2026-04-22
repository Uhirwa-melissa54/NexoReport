package com.report.nexoreport.dto;

import jakarta.validation.constraints.NotBlank;

public class IssueResolveRequest {
    @NotBlank
    private String resolutionMessage;

    public String getResolutionMessage() {
        return resolutionMessage;
    }

    public void setResolutionMessage(String resolutionMessage) {
        this.resolutionMessage = resolutionMessage;
    }
}
