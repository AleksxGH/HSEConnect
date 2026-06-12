package org.example.hseconnect.entity;

import java.io.Serializable;
import java.util.Objects;

public class EventTargetFacultyId implements Serializable {

    private Long eventId;
    private Long facultyId;

    public EventTargetFacultyId() {}

    public EventTargetFacultyId(Long eventId, Long facultyId) {
        this.eventId = eventId;
        this.facultyId = facultyId;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTargetFacultyId that)) return false;
        return Objects.equals(eventId, that.eventId) && Objects.equals(facultyId, that.facultyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, facultyId);
    }
}
