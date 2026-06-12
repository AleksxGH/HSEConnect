package org.example.hseconnect.controller;

import org.example.hseconnect.model.NotificationDto;
import org.example.hseconnect.services.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public List<NotificationDto> getNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") Boolean read
    ) {
        return notificationService.getNotifications(userId, read);
    }

    @PutMapping("/{notificationId}/read/{userId}")
    public void markAsRead(
            @PathVariable Long notificationId,
            @PathVariable Long userId
    ) {
        notificationService.markAsRead(notificationId, userId);
    }

    @GetMapping("/{userId}/unread-count")
    public int countUnread(@PathVariable Long userId) {
        return notificationService.countUnread(userId);
    }

    @GetMapping("/user/{userId}/has-unread")
    public java.util.Map<String, Boolean> hasUnread(@PathVariable Long userId) {
        boolean hasUnread = notificationService.countUnread(userId) > 0;
        return java.util.Map.of("hasUnread", hasUnread);
    }
}