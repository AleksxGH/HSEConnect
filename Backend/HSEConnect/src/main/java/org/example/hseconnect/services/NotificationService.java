package org.example.hseconnect.services;

import org.example.hseconnect.model.NotificationDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class NotificationService {

    private final JdbcTemplate jdbcTemplate;

    public NotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationDto> getNotifications(Long userId, Boolean read) {
        String sql = """
            SELECT notification_id, notification_type, title, body,
                   related_event_id, related_user_id, related_friend_request_id,
                   is_read, created_at
            FROM app.notification
            WHERE user_id = ?
              AND is_read = ?
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new NotificationDto(
                rs.getLong("notification_id"),
                rs.getString("notification_type"),
                rs.getString("title"),
                rs.getString("body"),
                rs.getObject("related_event_id") == null ? null : rs.getLong("related_event_id"),
                rs.getObject("related_user_id") == null ? null : rs.getLong("related_user_id"),
                rs.getObject("related_friend_request_id") == null ? null : rs.getLong("related_friend_request_id"),
                rs.getBoolean("is_read"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), userId, read);
    }

    public void markAsRead(Long notificationId, Long userId) {
        jdbcTemplate.update("""
            UPDATE app.notification
            SET is_read = true
            WHERE notification_id = ?
              AND user_id = ?
        """, notificationId, userId);
    }

    public void createNotification(
            Long userId,
            String type,
            String title,
            String body,
            Long relatedEventId,
            Long relatedUserId,
            Long relatedFriendRequestId
    ) {
        jdbcTemplate.update("""
            INSERT INTO app.notification
            (user_id, notification_type, title, body,
             related_event_id, related_user_id, related_friend_request_id,
             is_read, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, false, ?)
        """, userId, type, title, body, relatedEventId, relatedUserId, relatedFriendRequestId, Timestamp.valueOf(
                LocalDateTime.now(ZoneId.of("Europe/Moscow"))
        ));
    }

    public int countUnread(Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM app.notification
            WHERE user_id = ?
              AND is_read = false
        """, Integer.class, userId);

        return count == null ? 0 : count;
    }
}