package org.example.hseconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "app", name = "event_participant")
@IdClass(EventParticipantId.class)
public class EventParticipant {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "participant_status")
    private String participantStatus;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public EventParticipant() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getParticipantStatus() { return participantStatus; }
    public void setParticipantStatus(String participantStatus) { this.participantStatus = participantStatus; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

}
