package org.example.hseconnect.repository;

import org.example.hseconnect.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    List<EventParticipant> findByUserId(Long userId);
    Optional<EventParticipant> findByEventIdAndUserId(Long eventId, Long userId);
}