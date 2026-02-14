package com.jobportal.dto;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        boolean emailVerified,
        String skills,
        Integer experienceYears,
        String education,
        String portfolioUrl
) {
}
