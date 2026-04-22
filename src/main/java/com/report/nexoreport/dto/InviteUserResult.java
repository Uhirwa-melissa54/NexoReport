package com.report.nexoreport.dto;

public class InviteUserResult {

    private boolean success;
    private String message;
    private Long userId;
    private boolean canResend;

    public InviteUserResult() {}

    public InviteUserResult(boolean success, String message, Long userId, boolean canResend) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.canResend = canResend;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isCanResend() {
        return canResend;
    }
}