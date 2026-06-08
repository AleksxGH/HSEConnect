package org.example.hseconnect.controller;

import org.example.hseconnect.model.EventDto;
import org.example.hseconnect.services.EventService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDto> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/my")
    public List<EventDto> getMyEvents() {
        return eventService.getMyEvents();
    }

    @GetMapping("/going")
    public List<EventDto> getGoingEvents() {
        return eventService.getGoingEvents();
    }

    @PostMapping
    public EventDto createEvent(@RequestBody EventDto event) {
        return eventService.createEvent(event);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId);
    }

    @PutMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long eventId, @RequestBody EventDto event) {
        return eventService.updateEvent(eventId, event);
    }

    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
    }

    @PostMapping("/{eventId}/respond")
    public ResponseEntity<?> respondToEvent(@PathVariable Long eventId) {
        try {
            Long currentUserId = 1L;
            EventDto event = eventService.respondToEvent(eventId, currentUserId);
            return ResponseEntity.ok(event);
        } catch (RuntimeException error) {
            return ResponseEntity.badRequest().body(error.getMessage());
        }
    }
}

