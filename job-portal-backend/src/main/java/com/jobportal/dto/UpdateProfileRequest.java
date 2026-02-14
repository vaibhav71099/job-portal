package com.jobportal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Size(max = 1000, message = "Skills should be under 1000 characters")
        String skills,

        @Min(value = 0, message = "Experience should be 0 or more")
        @Max(value = 60, message = "Experience seems invalid")
        Integer experienceYears,

        @Size(max = 500, message = "Education should be under 500 characters")
        String education,

        @Size(max = 500, message = "Portfolio URL should be under 500 characters")
        String portfolioUrl
) {
}
