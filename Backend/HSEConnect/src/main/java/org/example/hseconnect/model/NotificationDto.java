package org.example.hseconnect.model;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String body;
    private Long relatedEventId;
    private Long relatedUserId;
    private Long relatedFriendRequestId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationDto() {}

    public NotificationDto(Long id, String type, String title, String body,
                           Long relatedEventId, Long relatedUserId,
                           Long relatedFriendRequestId, Boolean isRead,
                           LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.relatedEventId = relatedEventId;
        this.relatedUserId = relatedUserId;
        this.relatedFriendRequestId = relatedFriendRequestId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Long getRelatedEventId() { return relatedEventId; }
    public Long getRelatedUserId() { return relatedUserId; }
    public Long getRelatedFriendRequestId() { return relatedFriendRequestId; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}