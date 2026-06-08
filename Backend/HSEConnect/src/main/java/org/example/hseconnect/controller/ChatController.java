package org.example.hseconnect.controller;

import org.example.hseconnect.model.ChatDto;
import org.example.hseconnect.model.MessageDto;
import org.example.hseconnect.model.SendMessageRequest;
import org.example.hseconnect.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatDto> getChats() {
        return chatService.getChats();
    }

    @GetMapping("/{chatId}/messages")
    public List<MessageDto> getMessages(@PathVariable Long chatId) {
        return chatService.getMessages(chatId);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long chatId,
            @RequestBody SendMessageRequest request
    ) {
        MessageDto message = chatService.sendMessage(chatId, request.getText());
        return ResponseEntity.ok(message);
    }
}