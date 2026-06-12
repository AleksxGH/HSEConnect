package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "event_target_faculty")
@IdClass(EventTargetFacultyId.class)
public class EventTargetFaculty {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "faculty_id")
    private Long facultyId;

    public EventTargetFaculty() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

}
