package org.example.hseconnect.services;

import org.example.hseconnect.model.EventDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EventService {
    private final List<EventDto> events = new ArrayList<>();
    private Long nextId = 7L;

    public EventService() {
        List<Long> respondedUserIds = new ArrayList<>();
        events.add(new EventDto(1L, "Подготовка к экзамену по матанализу", "Учёба", "Покровка", "2024-12-15", "18:00", "Совместная подготовка к экзамену", respondedUserIds));
        events.add(new EventDto(2L, "Футбольный матч ФКН vs ПМИ", "Спорт", "Стадион", "2024-12-21", "20:00", "Товарищеский матч между факультетами", respondedUserIds));
        events.add(new EventDto(3L, "IT-лекция: Будущее нейросетей", "Образование", "Атриум", "2024-12-28", "18:30", "Открытая лекция про развитие искусственного интеллекта", respondedUserIds));
        events.add(new EventDto(4L, "Новогодняя вечеринка в БК", "Вечеринка", "БК", "2024-12-31", "20:00", "Встречаем Новый год вместе", respondedUserIds));
        events.add(new EventDto(5L, "Настольные игры в кампусе", "Развлечения", "Коворкинг", "2025-01-10", "17:30", "Вечер настольных игр для студентов", respondedUserIds));
        events.add(new EventDto(6L, "Встреча первокурсников", "Общение", "Главный корпус", "2025-01-15", "16:00", "Неформальная встреча и знакомство", respondedUserIds));
    }

    public List<EventDto> getAllEvents() {
        events.sort(Comparator.comparing(EventDto::getDate).thenComparing(EventDto::getTime));
        return events;
    }

    public List<EventDto> getMyEvents() {
        return events.stream().limit(3).toList();
    }

    public List<EventDto> getGoingEvents() {
        return events.stream().skip(3).toList();
    }

    public EventDto createEvent(EventDto event) {
        event.setId(nextId++);
        events.add(event);
        return event;
    }

    public EventDto updateEvent(Long eventId, EventDto updatedEvent) {
        for (EventDto event : events) {
            if (event.getId().equals(eventId)) {
                event.setTitle(updatedEvent.getTitle());
                event.setType(updatedEvent.getType());
                event.setLocation(updatedEvent.getLocation());
                event.setDate(updatedEvent.getDate());
                event.setTime(updatedEvent.getTime());
                event.setDescription(updatedEvent.getDescription());
                return event;
            }
        }

        throw new RuntimeException("Event not found");
    }

    public void deleteEvent(Long eventId) {
        events.removeIf(event -> event.getId().equals(eventId));
    }

    public EventDto getEventById(Long eventId) {
        return events.stream()
                .filter(event -> event.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public EventDto respondToEvent(Long eventId, Long userId) {
        EventDto event = getEventById(eventId);

        if (event.getRespondedUserIds() == null) {
            event.setRespondedUserIds(new ArrayList<>());
        }

        if (event.getRespondedUserIds().contains(userId)) {
            throw new RuntimeException("Вы уже откликнулись на это событие");
        }

        event.getRespondedUserIds().add(userId);

        return event;
    }
}