package com.jobportal.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        UserDto user
) {
}
