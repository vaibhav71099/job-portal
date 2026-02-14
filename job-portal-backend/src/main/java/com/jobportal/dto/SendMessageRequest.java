package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull(message = "Receiver id is required")
        Long receiverId,

        @NotBlank(message = "Message content is required")
        @Size(max = 2000, message = "Message too long")
        String content
) {
}
