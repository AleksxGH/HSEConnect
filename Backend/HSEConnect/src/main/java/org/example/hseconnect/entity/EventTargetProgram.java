package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "event_target_program")
@IdClass(EventTargetProgramId.class)
public class EventTargetProgram {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "education_program_id")
    private Long educationProgramId;

    public EventTargetProgram() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getEducationProgramId() { return educationProgramId; }
    public void setEducationProgramId(Long educationProgramId) { this.educationProgramId = educationProgramId; }

}
