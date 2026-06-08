package org.example.hseconnect.model;

public class MessageDto {
    private Long id;
    private Long chatId;
    private String text;
    private String sender;
    private String time;

    public MessageDto(Long id, Long chatId, String text, String sender, String time) {
        this.id = id;
        this.chatId = chatId;
        this.text = text;
        this.sender = sender;
        this.time = time;
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public String getText() { return text; }
    public String getSender() { return sender; }
    public String getTime() { return time; }
}