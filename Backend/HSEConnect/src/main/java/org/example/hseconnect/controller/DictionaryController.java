package org.example.hseconnect.controller;

import org.example.hseconnect.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DictionaryController {

    private final ProfileService profileService;

    public DictionaryController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/api/interests")
    public ResponseEntity<?> getInterests(@RequestParam(required = false) Boolean approved) {
        return ResponseEntity.ok(profileService.getInterests(approved));
    }

    @PostMapping("/api/interests")
    public ResponseEntity<?> createInterest(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(profileService.createInterest(body));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/api/faculties")
    public ResponseEntity<?> getFaculties() {
        return ResponseEntity.ok(profileService.getFaculties());
    }

    @PostMapping("/api/faculties")
    public ResponseEntity<?> createFaculty(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(profileService.createFaculty(body));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/api/education-programs")
    public ResponseEntity<?> getEducationPrograms(@RequestParam(required = false) Long facultyId) {
        return ResponseEntity.ok(profileService.getEducationPrograms(facultyId));
    }

    @PostMapping("/api/education-programs")
    public ResponseEntity<?> createEducationProgram(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(profileService.createEducationProgram(body));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }


}