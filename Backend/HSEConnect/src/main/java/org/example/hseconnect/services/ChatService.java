package org.example.hseconnect.services;

import org.example.hseconnect.model.ChatDto;
import org.example.hseconnect.model.MessageDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatService {

    private final List<ChatDto> chats = new ArrayList<>();
    private final Map<Long, List<MessageDto>> messages = new HashMap<>();
    private long messageIdCounter = 100;

    public ChatService() {
        chats.add(new ChatDto(1L, "Алексей Смирнов", "А", "онлайн", "Всё отлично, сегодня обсудим архитектуру"));
        chats.add(new ChatDto(2L, "Мария Ковальчук", "М", "была вчера", "Привет! Зацени статью по нейросетям"));
        chats.add(new ChatDto(3L, "Даниил Воробьёв", "Д", "печатает...", "В субботу футбол в 18:00, будешь?"));
        chats.add(new ChatDto(4L, "Екатерина Тихонова", "Е", "онлайн", "Хей! Нашёл баги в коде?"));

        messages.put(1L, new ArrayList<>(List.of(
                new MessageDto(1L, 1L, "Привет! Как дела с проектом?", "incoming", "12:45"),
                new MessageDto(2L, 1L, "Всё отлично, сегодня обсудим архитектуру", "outgoing", "12:47")
        )));

        messages.put(2L, new ArrayList<>(List.of(
                new MessageDto(3L, 2L, "Привет! Зацени статью по нейросетям", "incoming", "10:20")
        )));

        messages.put(3L, new ArrayList<>(List.of(
                new MessageDto(4L, 3L, "В субботу футбол в 18:00, будешь?", "incoming", "вчера")
        )));

        messages.put(4L, new ArrayList<>(List.of(
                new MessageDto(5L, 4L, "Хей! Нашёл баги в коде?", "incoming", "15:12")
        )));
    }

    public List<ChatDto> getChats() {
        return chats;
    }

    public List<MessageDto> getMessages(Long chatId) {
        if (!messages.containsKey(chatId)) {
            throw new RuntimeException("Чат не найден");
        }

        return messages.get(chatId);
    }

    public MessageDto sendMessage(Long chatId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Сообщение не может быть пустым");
        }

        if (!messages.containsKey(chatId)) {
            throw new RuntimeException("Чат не найден");
        }

        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        MessageDto newMessage = new MessageDto(
                ++messageIdCounter,
                chatId,
                text.trim(),
                "outgoing",
                time
        );

        messages.get(chatId).add(newMessage);
        updateLastMessage(chatId, text.trim());

        return newMessage;
    }

    private void updateLastMessage(Long chatId, String text) {
        for (int i = 0; i < chats.size(); i++) {
            ChatDto chat = chats.get(i);

            if (chat.getId().equals(chatId)) {
                chats.set(i, new ChatDto(
                        chat.getId(),
                        chat.getName(),
                        chat.getAvatarInitial(),
                        chat.getStatus(),
                        text
                ));
                return;
            }
        }
    }
}
