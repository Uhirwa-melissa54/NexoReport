package com.report.nexoreport.dto;

import jakarta.validation.constraints.NotBlank;

public class IssueCommentRequest {
    @NotBlank
    private String message;
    private boolean privateToCreator;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isPrivateToCreator() { return privateToCreator; }
    public void setPrivateToCreator(boolean privateToCreator) { this.privateToCreator = privateToCreator; }
}
