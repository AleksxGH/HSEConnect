package org.example.hseconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "app", name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "related_event_id")
    private Long relatedEventId;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(name = "related_friend_request_id")
    private Long relatedFriendRequestId;

    @Column(name = "is_read")
    private Boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Long getRelatedEventId() { return relatedEventId; }
    public void setRelatedEventId(Long relatedEventId) { this.relatedEventId = relatedEventId; }

    public Long getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Long relatedUserId) { this.relatedUserId = relatedUserId; }

    public Long getRelatedFriendRequestId() { return relatedFriendRequestId; }
    public void setRelatedFriendRequestId(Long relatedFriendRequestId) { this.relatedFriendRequestId = relatedFriendRequestId; }

    public Boolean isRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
