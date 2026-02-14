package com.jobportal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull(message = "jobId is required")
        Long jobId,

        @Size(max = 2048, message = "Resume text/link too long")
        String resume
) {
}
