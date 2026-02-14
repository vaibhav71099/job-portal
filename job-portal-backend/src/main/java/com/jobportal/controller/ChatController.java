package com.jobportal.controller;

import com.jobportal.dto.SendMessageRequest;
import com.jobportal.security.SecurityUtil;
import com.jobportal.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/chat", "/api/v1/chat"})
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public Object send(@Valid @RequestBody SendMessageRequest request) {
        var currentUser = SecurityUtil.currentUser();
        return chatService.send(currentUser.id(), request);
    }

    @GetMapping("/conversation/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Object conversation(@PathVariable Long userId) {
        var currentUser = SecurityUtil.currentUser();
        return chatService.conversation(currentUser.id(), userId);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> unreadCount() {
        var currentUser = SecurityUtil.currentUser();
        return Map.of("unread", chatService.unreadCount(currentUser.id()));
    }
}
