package com.jobportal.dto;

import jakarta.validation.constraints.NotNull;

public record ScheduleInterviewRequest(
        @NotNull(message = "applicationId is required")
        Long applicationId,

        @NotNull(message = "candidateId is required")
        Long candidateId,

        @NotNull(message = "scheduledAtEpochMs is required")
        Long scheduledAtEpochMs
) {
}
