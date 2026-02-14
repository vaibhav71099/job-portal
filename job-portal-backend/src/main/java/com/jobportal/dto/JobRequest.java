package com.jobportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Company is required")
        String company,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Salary is required")
        @Min(value = 0, message = "Salary must be positive")
        Double salary,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description
) {
}
