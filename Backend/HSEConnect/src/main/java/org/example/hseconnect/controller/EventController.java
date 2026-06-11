package org.example.hseconnect.controller;

import org.example.hseconnect.model.EventDto;
import org.example.hseconnect.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Long DEFAULT_USER_ID = 1L;

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<?> getEvents(@RequestParam(required = false) Long userId) {
        try {
            Long currentUserId = userId == null ? DEFAULT_USER_ID : userId;
            List<EventDto> events = eventService.getAllEvents(currentUserId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyEvents(@RequestParam Long userId) {
        try {
            List<EventDto> events = eventService.getMyEvents(userId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/going")
    public ResponseEntity<?> getGoingEvents(@RequestParam Long userId) {
        try {
            List<EventDto> events = eventService.getGoingEvents(userId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/user/{profileUserId}")
    public ResponseEntity<?> getUserEventsForViewer(
            @PathVariable Long profileUserId,
            @RequestParam Long viewerId
    ) {
        try {
            return ResponseEntity.ok(
                    eventService.getUserEventsForViewer(profileUserId, viewerId)
            );
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/user/{profileUserId}/going")
    public ResponseEntity<?> getUserGoingEventsForViewer(
            @PathVariable Long profileUserId,
            @RequestParam Long viewerId
    ) {
        try {
            return ResponseEntity.ok(
                    eventService.getUserGoingEventsForViewer(profileUserId, viewerId)
            );
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventDto event) {
        try {
            return ResponseEntity.ok(eventService.createEvent(event));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long viewerId
    ) {
        try {
            if (viewerId == null) {
                return ResponseEntity.ok(eventService.getEventById(eventId));
            }

            return ResponseEntity.ok(eventService.getEventByIdForViewer(eventId, viewerId));
        } catch (RuntimeException error) {
            return ResponseEntity.status(404).body(error.getMessage());
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long eventId,
            @RequestParam String title,
            @RequestParam String location,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String type,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String privacy,
            @RequestParam(required = false) MultipartFile photo
    ) {
        try {
            EventDto event = new EventDto();
            event.setTitle(title);
            event.setLocation(location);
            event.setDate(date);
            event.setTime(time);
            event.setType(type);
            event.setDescription(description);
            event.setPrivacy(privacy);

            EventDto updated = eventService.updateEvent(eventId, event);

            if (photo != null && !photo.isEmpty()) {
                eventService.uploadEventPhoto(eventId, photo);
                updated = eventService.getEventById(eventId);
            }

            return ResponseEntity.ok(updated);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", error.getMessage()));
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{eventId}/respond")
    public ResponseEntity<?> respondToEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long userId
    ) {
        try {
            Long currentUserId = userId == null ? DEFAULT_USER_ID : userId;
            EventDto event = eventService.respondToEvent(eventId, currentUserId);
            return ResponseEntity.ok(event);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @DeleteMapping("/{eventId}/respond")
    public ResponseEntity<?> cancelRespondToEvent(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        try {
            EventDto event = eventService.cancelRespondToEvent(eventId, userId);
            return ResponseEntity.ok(event);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventParticipants(eventId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{eventId}/invite")
    public ResponseEntity<?> inviteToEvent(
            @PathVariable Long eventId,
            @RequestParam Long inviterId,
            @RequestBody java.util.Map<String, Long> body
    ) {
        try {
            Long friendId = body.get("friendId");
            eventService.inviteToEvent(eventId, inviterId, friendId);
            return ResponseEntity.ok(java.util.Map.of("message", "Приглашение отправлено"));
        } catch (RuntimeException error) {
            return ResponseEntity.ok(java.util.Map.of("message", "Приглашение отклонено"));
        }
    }

    @PostMapping("/{eventId}/invite/accept")
    public ResponseEntity<?> acceptEventInvitation(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(eventService.acceptEventInvitation(eventId, userId));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }

    @PostMapping("/{eventId}/invite/decline")
    public ResponseEntity<?> declineEventInvitation(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        try {
            eventService.declineEventInvitation(eventId, userId);
            return ResponseEntity.ok(java.util.Map.of("message", "Приглашение отклонено"));
        } catch (RuntimeException error) {
            return ResponseEntity.ok(java.util.Map.of("message", "Приглашение отклонено"));
        }
    }

    @PostMapping("/{eventId}/photo")
    public ResponseEntity<?> uploadEventPhoto(
            @PathVariable Long eventId,
            @RequestParam("photo") MultipartFile photo
    ) {
        try {
            String photoUrl = eventService.uploadEventPhoto(eventId, photo);
            return ResponseEntity.ok(java.util.Map.of("photoUrl", photoUrl));
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", error.getMessage()));
        }
    }
}