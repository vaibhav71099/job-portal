package com.jobportal.websocket;

public record ChatSocketMessage(
        Long senderId,
        Long receiverId,
        String content
) {
}
