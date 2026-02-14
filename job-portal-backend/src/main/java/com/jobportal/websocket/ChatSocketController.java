package com.jobportal.websocket;

import com.jobportal.dto.SendMessageRequest;
import com.jobportal.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(ChatSocketMessage payload) {
        Long senderId = payload.senderId();
        if (senderId == null || payload.receiverId() == null) {
            return;
        }
        var saved = chatService.send(senderId, new SendMessageRequest(payload.receiverId(), payload.content()));
        messagingTemplate.convertAndSend("/topic/chat." + payload.receiverId(), saved);
        messagingTemplate.convertAndSend("/topic/chat." + senderId, saved);
    }
}
