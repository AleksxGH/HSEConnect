package org.example.hseconnect.controller;

import org.example.hseconnect.services.FriendsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
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
}