package org.example.hseconnect.repository;

import org.example.hseconnect.entity.EventAccessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventAccessTypeRepository extends JpaRepository<EventAccessType, Long> {
    Optional<EventAccessType> findByName(String name);
}