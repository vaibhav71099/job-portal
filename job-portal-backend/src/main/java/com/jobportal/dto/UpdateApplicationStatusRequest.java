package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateApplicationStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "APPLIED|SHORTLISTED|REJECTED|HIRED", message = "Invalid status")
        String status
) {
}
