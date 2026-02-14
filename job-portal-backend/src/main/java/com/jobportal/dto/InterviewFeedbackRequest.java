package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InterviewFeedbackRequest(
        @Size(max = 1000, message = "Feedback too long")
        String feedback,

        @NotBlank(message = "Status is required")
        String status
) {
}
