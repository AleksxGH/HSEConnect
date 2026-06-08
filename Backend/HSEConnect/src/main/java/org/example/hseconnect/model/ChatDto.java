package org.example.hseconnect.model;

public class ChatDto {
    private Long id;
    private String name;
    private String avatarInitial;
    private String status;
    private String lastMessage;

    public ChatDto(Long id, String name, String avatarInitial, String status, String lastMessage) {
        this.id = id;
        this.name = name;
        this.avatarInitial = avatarInitial;
        this.status = status;
        this.lastMessage = lastMessage;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAvatarInitial() { return avatarInitial; }
    public String getStatus() { return status; }
    public String getLastMessage() { return lastMessage; }
}