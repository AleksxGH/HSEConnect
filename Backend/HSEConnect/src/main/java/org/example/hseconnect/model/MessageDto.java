package org.example.hseconnect.model;

import java.util.List;

public class MessageDto {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String text;
    private String sender;
    private String time;
    private String createdAt;
    private String editedAt;
    private String deletedAt;
    private List<AttachmentDto> attachments;

    public MessageDto() {}

    public MessageDto(Long id, Long chatId, String text, String sender, String time) {
        this.id = id;
        this.chatId = chatId;
        this.text = text;
        this.sender = sender;
        this.time = time;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getEditedAt() { return editedAt; }
    public void setEditedAt(String editedAt) { this.editedAt = editedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }
}
