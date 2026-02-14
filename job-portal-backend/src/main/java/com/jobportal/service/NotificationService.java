package com.jobportal.service;

import com.jobportal.exception.ApiException;
import com.jobportal.model.Notification;
import com.jobportal.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(Long userId, String type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<Notification> myNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Notification> myNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public Notification markRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ApiException("Not allowed to modify this notification");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public long markAllRead(Long userId) {
        List<Notification> items = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        items.forEach(item -> item.setRead(true));
        notificationRepository.saveAll(items);
        return items.size();
    }
}
