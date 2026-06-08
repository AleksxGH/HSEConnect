package org.example.hseconnect.controller;

import org.example.hseconnect.model.ProfileDto;
import org.example.hseconnect.services.ProfileService;
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
    public ProfileDto getProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }

    @PostMapping("/{userId}/questionnaire")
    public ProfileDto saveQuestionnaire(
            @PathVariable Long userId,
            @RequestBody ProfileDto profileDto
    ) {
        return profileService.saveQuestionnaire(userId, profileDto);
    }

    @PutMapping("/{userId}")
    public ProfileDto updateProfile(
            @PathVariable Long userId,
            @RequestBody ProfileDto profileDto
    ) {
        return profileService.updateProfile(userId, profileDto);
    }
}