package org.example.hseconnect.services;

import org.example.hseconnect.model.EventDto;
import org.example.hseconnect.model.FriendUserDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EventService {

    private static final String DEFAULT_ACCESS_TYPE = "public";

    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;
    private final BlockService blockService;

    public EventService(JdbcTemplate jdbcTemplate, NotificationService notificationService, BlockService blockService) {
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
        this.blockService = blockService;
    }

    public List<EventDto> getAllEvents(Long currentUserId) {
        return jdbcTemplate.query("""
        SELECT e.event_id,
               e.creator_id,
               e.title,
               COALESCE(ec.name, '') AS type,
               COALESCE(a.full_address, '') AS location,
               e.starts_at,
               e.description,
               (
                   SELECT COUNT(*)
                   FROM app.event_participant ep
                   WHERE ep.event_id = e.event_id
                     AND ep.cancelled_at IS NULL
               ) AS participants_count
        FROM app.event e
        LEFT JOIN app.event_category ec ON ec.event_category_id = e.category_id
        LEFT JOIN app.address a ON a.address_id = e.address_id
        WHERE NOT EXISTS (
            SELECT 1
            FROM app.user_block ub
            WHERE (ub.blocker_id = ? AND ub.blocked_id = e.creator_id)
               OR (ub.blocker_id = e.creator_id AND ub.blocked_id = ?)
        )
        ORDER BY e.starts_at
    """, eventMapper(), currentUserId, currentUserId);
    }

    public List<EventDto> getMyEvents(Long userId) {
        return jdbcTemplate.query("""
        SELECT e.event_id,
               e.creator_id,
               e.title,
               COALESCE(ec.name, '') AS type,
               COALESCE(a.full_address, '') AS location,
               e.starts_at,
               e.description,
               (
                   SELECT COUNT(*)
                   FROM app.event_participant ep
                   WHERE ep.event_id = e.event_id
                     AND ep.cancelled_at IS NULL
               ) AS participants_count
        FROM app.event e
        LEFT JOIN app.event_category ec ON ec.event_category_id = e.category_id
        LEFT JOIN app.address a ON a.address_id = e.address_id
        WHERE e.creator_id = ?
        ORDER BY e.starts_at
    """, eventMapper(), userId);
    }

    public List<EventDto> getGoingEvents(Long userId) {
        return jdbcTemplate.query("""
        SELECT e.event_id,
               e.creator_id,
               e.title,
               COALESCE(ec.name, '') AS type,
               COALESCE(a.full_address, '') AS location,
               e.starts_at,
               e.description,
               (
                   SELECT COUNT(*)
                   FROM app.event_participant ep_count
                   WHERE ep_count.event_id = e.event_id
                     AND ep_count.cancelled_at IS NULL
               ) AS participants_count
        FROM app.event e
        JOIN app.event_participant ep ON ep.event_id = e.event_id
        LEFT JOIN app.event_category ec ON ec.event_category_id = e.category_id
        LEFT JOIN app.address a ON a.address_id = e.address_id
        WHERE ep.user_id = ?
          AND ep.cancelled_at IS NULL
          AND NOT EXISTS (
              SELECT 1
              FROM app.user_block ub
              WHERE (ub.blocker_id = ? AND ub.blocked_id = e.creator_id)
                 OR (ub.blocker_id = e.creator_id AND ub.blocked_id = ?)
          )
        ORDER BY e.starts_at
    """, eventMapper(), userId, userId, userId);
    }

    @Transactional
    public EventDto createEvent(EventDto event) {
        validateEvent(event);

        Long categoryId = findOrCreateSimple("event_category", "event_category_id", normalize(event.getType(), "Другое"));
        String accessType = normalize(event.getPrivacy(), DEFAULT_ACCESS_TYPE);
        Long accessTypeId = findOrCreateSimple("event_access_type", "access_type_id", accessType);
        Long addressId = createAddressIfPresent(event.getLocation());
        LocalDateTime startsAt = parseStartsAt(event.getDate(), event.getTime());
        LocalDateTime now = LocalDateTime.now();

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (event.getCreatorId() == null || event.getCreatorId() <= 0) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO app.event
                    (creator_id, category_id, access_type_id, address_id, title, description,
                     starts_at, ends_at, max_participants, status, visibility, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, NULL, NULL, ?, ?, ?, ?)
                    """, new String[]{"event_id"});

            ps.setLong(1, event.getCreatorId());
            ps.setLong(2, categoryId);
            ps.setLong(3, accessTypeId);
            if (addressId == null) ps.setObject(4, null); else ps.setLong(4, addressId);
            ps.setString(5, event.getTitle().trim());
            ps.setString(6, event.getDescription());
            ps.setTimestamp(7, Timestamp.valueOf(startsAt));
            ps.setString(8, "active");
            ps.setString(9, "visible");
            ps.setTimestamp(10, Timestamp.valueOf(now));
            ps.setTimestamp(11, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        event.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        createInvitations(event.getId(), event.getCreatorId(), event.getInvitedFriends());
        return getEventById(event.getId());
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto updatedEvent) {
        validateEvent(updatedEvent);

        Long categoryId = findOrCreateSimple("event_category", "event_category_id", normalize(updatedEvent.getType(), "Другое"));
        Long addressId = createAddressIfPresent(updatedEvent.getLocation());
        LocalDateTime startsAt = parseStartsAt(updatedEvent.getDate(), updatedEvent.getTime());

        int updated = jdbcTemplate.update("""
                UPDATE app.event
                SET category_id = ?,
                    address_id = ?,
                    title = ?,
                    description = ?,
                    starts_at = ?,
                    updated_at = NOW()
                WHERE event_id = ?
                """, categoryId, addressId, updatedEvent.getTitle().trim(), updatedEvent.getDescription(), startsAt, eventId);

        if (updated == 0) {
            throw new RuntimeException("Событие не найдено");
        }

        return getEventById(eventId);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        jdbcTemplate.update("DELETE FROM app.event_participant WHERE event_id = ?", eventId);
        jdbcTemplate.update("DELETE FROM app.event_invitation WHERE event_id = ?", eventId);
        jdbcTemplate.update("DELETE FROM app.event_target_faculty WHERE event_id = ?", eventId);
        jdbcTemplate.update("DELETE FROM app.event_target_program WHERE event_id = ?", eventId);
        jdbcTemplate.update("DELETE FROM app.chat WHERE event_id = ?", eventId);

        int deleted = jdbcTemplate.update("DELETE FROM app.event WHERE event_id = ?", eventId);
        if (deleted == 0) {
            throw new RuntimeException("Событие не найдено");
        }
    }

    @Transactional
    public EventDto cancelRespondToEvent(Long eventId, Long userId) {

        Integer exists = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM app.event_participant
            WHERE event_id = ? AND user_id = ? AND cancelled_at IS NULL
            """,
                Integer.class,
                eventId,
                userId
        );

        if (exists == null || exists == 0) {
            throw new RuntimeException("Вы не откликались на это событие");
        }

        jdbcTemplate.update("""
            DELETE FROM app.event_participant
            WHERE event_id = ? AND user_id = ?
            """,
                eventId,
                userId
        );

        return getEventById(eventId);
    }

    public EventDto getEventById(Long eventId) {
        List<EventDto> result = jdbcTemplate.query("""
        SELECT e.event_id,
               e.creator_id,
               e.title,
               COALESCE(ec.name, '') AS type,
               COALESCE(a.full_address, '') AS location,
               e.starts_at,
               e.description,
               (
                   SELECT COUNT(*)
                   FROM app.event_participant ep
                   WHERE ep.event_id = e.event_id
                     AND ep.cancelled_at IS NULL
               ) AS participants_count
        FROM app.event e
        LEFT JOIN app.event_category ec ON ec.event_category_id = e.category_id
        LEFT JOIN app.address a ON a.address_id = e.address_id
        WHERE e.event_id = ?
    """, eventMapper(), eventId);

        if (result.isEmpty()) {
            throw new RuntimeException("Событие не найдено");
        }

        return result.get(0);
    }

    public EventDto getEventByIdForViewer(Long eventId, Long viewerId) {
        EventDto event = getEventById(eventId);

        if (!event.getCreatorId().equals(viewerId)
                && blockService.hasBlockBetween(viewerId, event.getCreatorId())) {
            throw new RuntimeException("Событие недоступно");
        }

        return event;
    }

    @Transactional
    public EventDto respondToEvent(Long eventId, Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        EventDto event = getEventById(eventId);

        if (!event.getCreatorId().equals(userId)
                && blockService.hasBlockBetween(userId, event.getCreatorId())) {
            throw new RuntimeException("Вы не можете откликнуться на это событие");
        }

        Integer exists = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM app.event_participant
                WHERE event_id = ? AND user_id = ?
                """, Integer.class, eventId, userId);

        if (exists != null && exists > 0) {
            throw new RuntimeException("Вы уже откликнулись на это событие");
        }

        jdbcTemplate.update("""
                INSERT INTO app.event_participant (event_id, user_id, participant_status, joined_at, cancelled_at)
                VALUES (?, ?, ?, NOW(), NULL)
                """, eventId, userId, "going");

        Long creatorId = getEventCreatorId(eventId);

        if (!creatorId.equals(userId)) {
            String userName = getUserName(userId);
            String eventTitle = getEventTitle(eventId);

            notificationService.createNotification(
                    creatorId,
                    "event_going",
                    "Новый отклик на событие",
                    userName + " откликнулся на ваше событие «" + eventTitle + "»",
                    eventId,
                    userId,
                    null
            );
        }

        return getEventById(eventId);
    }

    private RowMapper<EventDto> eventMapper() {
        return (rs, rowNum) -> {
            Timestamp timestamp = rs.getTimestamp("starts_at");
            LocalDateTime startsAt = timestamp == null ? null : timestamp.toLocalDateTime();

            EventDto dto = new EventDto();

            dto.setId(rs.getLong("event_id"));
            dto.setCreatorId(rs.getLong("creator_id"));
            dto.setTitle(rs.getString("title"));
            dto.setType(rs.getString("type"));
            dto.setLocation(rs.getString("location"));
            dto.setDescription(rs.getString("description"));
            dto.setParticipantsCount(rs.getInt("participants_count"));


            if (startsAt != null) {
                dto.setDate(startsAt.toLocalDate().toString());
                dto.setTime(startsAt.toLocalTime().toString().substring(0, 5));
            }

            dto.setRespondedUserIds(getRespondedUserIds(dto.getId()));

            return dto;
        };
    }

    private List<Long> getRespondedUserIds(Long eventId) {
        return jdbcTemplate.queryForList("""
                SELECT user_id
                FROM app.event_participant
                WHERE event_id = ? AND cancelled_at IS NULL
                """, Long.class, eventId);
    }

    private Long findOrCreateSimple(String tableName, String idColumn, String name) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT " + idColumn + " FROM app." + tableName + " WHERE LOWER(name) = LOWER(?) LIMIT 1",
                Long.class,
                name
        );

        if (!ids.isEmpty()) return ids.get(0);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app." + tableName + " (name) VALUES (?)",
                    new String[]{idColumn}
            );
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private void createInvitations(Long eventId, Long creatorId, List<Long> invitedFriends) {
        if (invitedFriends == null || invitedFriends.isEmpty()) {
            return;
        }

        String eventTitle = getEventTitle(eventId);
        String creatorName = getUserName(creatorId);

        for (Long invitedUserId : invitedFriends) {
            if (invitedUserId == null || invitedUserId.equals(creatorId)) {
                continue;
            }

            if (blockService.hasBlockBetween(creatorId, invitedUserId)) {
                continue;
            }

            jdbcTemplate.update("""
            INSERT INTO app.event_invitation
            (event_id, inviter_user_id, invitee_user_id, invitation_status, invited_at)
            VALUES (?, ?, ?, ?, NOW())
        """, eventId, creatorId, invitedUserId, "pending");

            notificationService.createNotification(
                    invitedUserId,
                    "event_invitation",
                    "Приглашение на событие",
                    creatorName + " приглашает вас на событие «" + eventTitle + "»",
                    eventId,
                    creatorId,
                    null
            );
        }
    }

    private Long createAddressIfPresent(String location) {
        if (location == null || location.isBlank()) return null;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO app.address (campus_id, city, street, building, room, full_address, latitude, longitude)
                    VALUES (NULL, '', NULL, NULL, NULL, ?, NULL, NULL)
                    """, new String[]{"address_id"});
            ps.setString(1, location.trim());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public List<FriendUserDto> getEventParticipants(Long eventId) {
        return jdbcTemplate.query("""
        SELECT ep.user_id,
               COALESCE(CONCAT_WS(' ', p.first_name, p.last_name), 'Пользователь') AS name,
               COALESCE(p.avatar_url, '') AS avatar
        FROM app.event_participant ep
        LEFT JOIN app.profile p ON p.user_id = ep.user_id
        WHERE ep.event_id = ?
          AND ep.cancelled_at IS NULL
        ORDER BY ep.joined_at
        """,
                (rs, rowNum) -> new FriendUserDto(
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("avatar"),
                        "",
                        false,
                        0
                ),
                eventId
        );
    }

    private LocalDateTime parseStartsAt(String date, String time) {
        if (date == null || date.isBlank()) throw new RuntimeException("Дата события обязательна");
        if (time == null || time.isBlank()) throw new RuntimeException("Время события обязательно");

        return LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
    }

    private void validateEvent(EventDto event) {
        if (event == null) throw new RuntimeException("Событие не заполнено");
        if (event.getTitle() == null || event.getTitle().isBlank()) throw new RuntimeException("Название события обязательно");
        parseStartsAt(event.getDate(), event.getTime());
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private Long getEventCreatorId(Long eventId) {
        return jdbcTemplate.queryForObject("""
        SELECT creator_id
        FROM app.event
        WHERE event_id = ?
    """, Long.class, eventId);
    }

    private String getEventTitle(Long eventId) {
        return jdbcTemplate.queryForObject("""
        SELECT title
        FROM app.event
        WHERE event_id = ?
    """, String.class, eventId);
    }

    private String getUserName(Long userId) {
        List<String> names = jdbcTemplate.queryForList("""
        SELECT CONCAT_WS(' ', first_name, last_name)
        FROM app.profile
        WHERE user_id = ?
    """, String.class, userId);

        return names.isEmpty() ? "Пользователь" : names.get(0);
    }

    public List<EventDto> getUserEventsForViewer(Long profileUserId, Long viewerId) {
        if (!profileUserId.equals(viewerId)
                && blockService.hasBlockBetween(profileUserId, viewerId)) {
            throw new RuntimeException("События пользователя недоступны");
        }

        return getMyEvents(profileUserId);
    }

    public List<EventDto> getUserGoingEventsForViewer(Long profileUserId, Long viewerId) {
        if (!profileUserId.equals(viewerId)
                && blockService.hasBlockBetween(profileUserId, viewerId)) {
            throw new RuntimeException("События пользователя недоступны");
        }

        return getGoingEvents(profileUserId);
    }
}
