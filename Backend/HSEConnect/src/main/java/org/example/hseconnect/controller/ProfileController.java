package org.example.hseconnect.controller;

import org.example.hseconnect.model.ProfileDto;
import org.example.hseconnect.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(profileService.getProfileByUserId(userId));
        } catch (RuntimeException error) {
            return ResponseEntity.status(404).body(error.getMessage());
        }
    }

    @PostMapping("/{userId}/questionnaire")
    public ResponseEntity<?> saveQuestionnaire(
            @PathVariable Long userId,
            @RequestBody ProfileDto profileDto
    ) {
        try {
            return ResponseEntity.ok(profileService.saveQuestionnaire(userId, profileDto));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody ProfileDto profileDto
    ) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(userId, profileDto));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/suggest/{dictionary}")
    public ResponseEntity<?> suggest(
            @PathVariable String dictionary,
            @RequestParam(defaultValue = "") String q
    ) {
        try {
            return ResponseEntity.ok(profileService.suggest(dictionary, q));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{profileUserId}/viewer/{viewerId}")
    public ProfileDto getProfileForViewer(
            @PathVariable Long profileUserId,
            @PathVariable Long viewerId
    ) {
        return profileService.getProfileForViewer(viewerId, profileUserId);
    }


}