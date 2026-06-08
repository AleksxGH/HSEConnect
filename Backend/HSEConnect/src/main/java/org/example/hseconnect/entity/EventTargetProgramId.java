package org.example.hseconnect.entity;

import java.io.Serializable;
import java.util.Objects;

public class EventTargetProgramId implements Serializable {

    private Long eventId;
    private Long educationProgramId;

    public EventTargetProgramId() {}

    public EventTargetProgramId(Long eventId, Long educationProgramId) {
        this.eventId = eventId;
        this.educationProgramId = educationProgramId;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getEducationProgramId() { return educationProgramId; }
    public void setEducationProgramId(Long educationProgramId) { this.educationProgramId = educationProgramId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTargetProgramId that)) return false;
        return Objects.equals(eventId, that.eventId) && Objects.equals(educationProgramId, that.educationProgramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, educationProgramId);
    }
}
