package org.example.hseconnect.controller;

import org.example.hseconnect.model.FriendUserDto;
import org.example.hseconnect.services.FriendsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    private final FriendsService friendsService;

    public FriendsController(FriendsService friendsService) {
        this.friendsService = friendsService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getFriends(@PathVariable Long userId) {
        try {
            List<FriendUserDto> result = friendsService.getFriends(userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(friendsService.getFollowers(userId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(friendsService.getFollowing(userId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{userId}/follow/{targetUserId}")
    public ResponseEntity<?> follow(@PathVariable Long userId, @PathVariable Long targetUserId) {
        try {
            friendsService.follow(userId, targetUserId);
            return ResponseEntity.ok("Подписка оформлена");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @DeleteMapping("/{userId}/follow/{targetUserId}")
    public ResponseEntity<?> unfollow(@PathVariable Long userId, @PathVariable Long targetUserId) {
        try {
            friendsService.unfollow(userId, targetUserId);
            return ResponseEntity.ok("Вы отписались");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{userId}/add/{targetUserId}")
    public ResponseEntity<?> addFriend(@PathVariable Long userId, @PathVariable Long targetUserId) {
        try {
            friendsService.addFriend(userId, targetUserId);
            return ResponseEntity.ok("Ваша заявка отправлена");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @DeleteMapping("/{userId}/friend/{targetUserId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId, @PathVariable Long targetUserId) {
        try {
            friendsService.removeFriend(userId, targetUserId);
            return ResponseEntity.ok("Пользователь удалён из друзей");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @DeleteMapping("/{userId}/follower/{followerId}")
    public ResponseEntity<?> removeFollower(@PathVariable Long userId, @PathVariable Long followerId) {
        try {
            friendsService.removeFollower(userId, followerId);
            return ResponseEntity.ok("Подписчик удалён");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{userId}/status/{targetUserId}")
    public ResponseEntity<?> getRelationStatus(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        try {
            return ResponseEntity.ok(friendsService.getRelationStatus(userId, targetUserId));
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

    @PostMapping("/{userId}/accept/{targetUserId}")
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        try {
            friendsService.acceptFriendRequest(userId, targetUserId);
            return ResponseEntity.ok("Заявка принята");
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }
}