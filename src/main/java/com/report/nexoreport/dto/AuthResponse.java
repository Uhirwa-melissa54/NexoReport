package com.report.nexoreport.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
