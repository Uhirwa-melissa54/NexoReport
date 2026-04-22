package com.report.nexoreport.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
