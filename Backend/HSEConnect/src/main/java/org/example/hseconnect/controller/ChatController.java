package org.example.hseconnect.controller;

import org.example.hseconnect.model.ChatDto;
import org.example.hseconnect.model.MessageDto;
import org.example.hseconnect.model.SendMessageRequest;
import org.example.hseconnect.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getChats(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(chatService.getChats(userId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{chatId}/messages/user/{userId}")
    public ResponseEntity<?> getMessages(
            @PathVariable Long chatId,
            @PathVariable Long userId
    ) {
        try {
            return ResponseEntity.ok(chatService.getMessages(chatId, userId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{chatId}/messages/user/{userId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @RequestBody SendMessageRequest request
    ) {
        try {
            return ResponseEntity.ok(chatService.sendMessage(chatId, userId, request.getText()));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PutMapping("/{chatId}/read/user/{currentUserId}")
    public void markChatAsRead(
            @PathVariable Long chatId,
            @PathVariable Long currentUserId
    ) {
        chatService.markChatAsRead(chatId, currentUserId);
    }

    @PostMapping("/private/{userId}/{targetUserId}")
    public ResponseEntity<?> getOrCreatePrivateChat(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        try {
            ChatDto chat = chatService.getOrCreatePrivateChat(userId, targetUserId);
            return ResponseEntity.ok(chat);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{chatId}/messages/user/{userId}/with-files")
    public ResponseEntity<?> sendMessageWithFiles(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            return ResponseEntity.ok(chatService.sendMessageWithFiles(chatId, userId, text, files));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

}
