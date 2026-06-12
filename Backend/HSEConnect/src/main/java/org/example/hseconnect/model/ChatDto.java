package org.example.hseconnect.model;

public class ChatDto {
    private Long id;
    private String chatType;
    private Long eventId;
    private String createdAt;

    private String name;
    private String avatarInitial;
    private String status;
    private String lastMessage;

    public ChatDto() {}

    public ChatDto(Long id, String name, String avatarInitial, String status, String lastMessage) {
        this.id = id;
        this.name = name;
        this.avatarInitial = avatarInitial;
        this.status = status;
        this.lastMessage = lastMessage;
    }

    public ChatDto(Long id, String name, String avatarInitial, String status, String lastMessage, Integer unreadCount) {
        this.id = id;
        this.name = name;
        this.avatarInitial = avatarInitial;
        this.status = status;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarInitial() { return avatarInitial; }
    public void setAvatarInitial(String avatarInitial) { this.avatarInitial = avatarInitial; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    private Integer unreadCount = 0;

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    private Long otherUserId;

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }
}
