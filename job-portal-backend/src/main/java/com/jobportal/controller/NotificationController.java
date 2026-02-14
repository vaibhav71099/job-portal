package com.jobportal.controller;

import com.jobportal.security.SecurityUtil;
import com.jobportal.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/notifications", "/api/v1/notifications"})
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Object mine(@RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size) {
        var currentUser = SecurityUtil.currentUser();
        if (page != null && size != null) {
            return notificationService.myNotifications(currentUser.id(), page, size);
        }
        return notificationService.myNotifications(currentUser.id());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> unreadCount() {
        var currentUser = SecurityUtil.currentUser();
        return Map.of("unread", notificationService.unreadCount(currentUser.id()));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Object markRead(@PathVariable Long id) {
        var currentUser = SecurityUtil.currentUser();
        return notificationService.markRead(id, currentUser.id());
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> markAllRead() {
        var currentUser = SecurityUtil.currentUser();
        return Map.of("updated", notificationService.markAllRead(currentUser.id()));
    }
}
