package com.jobportal.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String role
) {
}
