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
    public ResponseEntity<?> getChats() {
        try {
            List<ChatDto> chats = chatService.getChats();
            return ResponseEntity.ok(chats);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long chatId) {
        try {
            List<MessageDto> messages = chatService.getMessages(chatId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long chatId,
            @RequestBody SendMessageRequest request
    ) {
        try {
            MessageDto message = chatService.sendMessage(chatId, request.getText());
            return ResponseEntity.ok(message);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }
}
