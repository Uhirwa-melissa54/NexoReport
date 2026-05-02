package com.report.nexoreport.dto;

public class InviteUserResult {

    private boolean success;
    private String message;
    private Long userId;
    private boolean canResend;
    private String email;
    private String temporaryPassword;

    public InviteUserResult() {}

    public InviteUserResult(boolean success, String message, Long userId, boolean canResend, String email, String temporaryPassword) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.canResend = canResend;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getUserId() { return userId; }
    public boolean isCanResend() { return canResend; }
    public String getEmail() { return email; }
    public String getTemporaryPassword() { return temporaryPassword; }
}
