package com.jobportal.service;

import com.jobportal.dto.SendMessageRequest;
import com.jobportal.model.ChatMessage;
import com.jobportal.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final NotificationService notificationService;

    public ChatService(ChatMessageRepository chatMessageRepository, NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.notificationService = notificationService;
    }

    public ChatMessage send(Long senderId, SendMessageRequest request) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(request.receiverId());
        message.setContent(request.content().trim());

        ChatMessage saved = chatMessageRepository.save(message);
        notificationService.create(
                request.receiverId(),
                "CHAT",
                "New message from user #" + senderId
        );
        return saved;
    }

    public List<ChatMessage> conversation(Long me, Long otherUserId) {
        return chatMessageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                me, otherUserId, otherUserId, me
        );
    }

    public long unreadCount(Long userId) {
        return chatMessageRepository.countByReceiverIdAndReadFalse(userId);
    }
}
