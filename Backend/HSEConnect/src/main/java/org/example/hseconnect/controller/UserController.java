package org.example.hseconnect.controller;

import org.example.hseconnect.services.FriendsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final FriendsService friendsService;

    public UserController(FriendsService friendsService) {
        this.friendsService = friendsService;
    }

    @GetMapping("/all/{currentUserId}")
    public ResponseEntity<?> getAllUsers(@PathVariable Long currentUserId) {
        try {
            return ResponseEntity.ok(friendsService.getAllUsers(currentUserId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String avatarUrl = friendsService.uploadAvatar(userId, file);
            return ResponseEntity.ok(avatarUrl);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<?> getUserAvatar(@PathVariable Long userId) {
        try {
            String avatarUrl = friendsService.getUserAvatar(userId);
            return ResponseEntity.ok(avatarUrl);
        } catch (RuntimeException error) {
            return ResponseEntity.notFound().build();
        }
    }
}