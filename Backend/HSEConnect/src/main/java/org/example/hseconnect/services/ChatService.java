package org.example.hseconnect.services;

import org.example.hseconnect.model.ChatDto;
import org.example.hseconnect.model.MessageDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public ChatService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ChatDto> getChats() {
        return jdbcTemplate.query("""
                SELECT c.chat_id,
                       COALESCE(e.title, 'Чат #' || c.chat_id) AS name,
                       COALESCE(last_message.message_text, '') AS last_message
                FROM app.chat c
                LEFT JOIN app.event e ON e.event_id = c.event_id
                LEFT JOIN LATERAL (
                    SELECT m.message_text
                    FROM app.message m
                    WHERE m.chat_id = c.chat_id AND m.deleted_at IS NULL
                    ORDER BY m.created_at DESC
                    LIMIT 1
                ) last_message ON TRUE
                ORDER BY c.created_at DESC
                """, (rs, rowNum) -> {
            String name = rs.getString("name");
            String avatarInitial = name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();

            return new ChatDto(
                    rs.getLong("chat_id"),
                    name,
                    avatarInitial,
                    "онлайн",
                    rs.getString("last_message")
            );
        });
    }

    public List<MessageDto> getMessages(Long chatId) {
        validateChatExists(chatId);

        return jdbcTemplate.query("""
                SELECT m.message_id,
                       m.chat_id,
                       m.sender_id,
                       m.message_text,
                       m.created_at
                FROM app.message m
                WHERE m.chat_id = ? AND m.deleted_at IS NULL
                ORDER BY m.created_at
                """, (rs, rowNum) -> {
            Long senderId = rs.getLong("sender_id");
            Timestamp createdAt = rs.getTimestamp("created_at");
            String time = createdAt == null
                    ? ""
                    : createdAt.toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

            return new MessageDto(
                    rs.getLong("message_id"),
                    rs.getLong("chat_id"),
                    rs.getString("message_text"),
                    DEFAULT_USER_ID.equals(senderId) ? "outgoing" : "incoming",
                    time
            );
        }, chatId);
    }

    @Transactional
    public MessageDto sendMessage(Long chatId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Сообщение не может быть пустым");
        }

        validateChatExists(chatId);

        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO app.message (chat_id, sender_id, message_text, created_at, edited_at, deleted_at)
                    VALUES (?, ?, ?, ?, NULL, NULL)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, chatId);
            ps.setLong(2, DEFAULT_USER_ID);
            ps.setString(3, text.trim());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        return new MessageDto(
                Objects.requireNonNull(keyHolder.getKey()).longValue(),
                chatId,
                text.trim(),
                "outgoing",
                now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    private void validateChatExists(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new RuntimeException("Чат не найден");
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app.chat WHERE chat_id = ?",
                Integer.class,
                chatId
        );

        if (count == null || count == 0) {
            throw new RuntimeException("Чат не найден");
        }
    }

    public ChatDto getOrCreatePrivateChat(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new RuntimeException("Нельзя открыть чат с самим собой");
        }

        List<Long> existing = jdbcTemplate.queryForList("""
        SELECT c.chat_id
        FROM app.chat c
        JOIN app.chat_participant cp1 ON cp1.chat_id = c.chat_id
        JOIN app.chat_participant cp2 ON cp2.chat_id = c.chat_id
        WHERE c.chat_type = 'PRIVATE'
          AND cp1.user_id = ?
          AND cp2.user_id = ?
          AND cp1.left_at IS NULL
          AND cp2.left_at IS NULL
        LIMIT 1
    """, Long.class, userId, targetUserId);

        Long chatId;

        if (!existing.isEmpty()) {
            chatId = existing.get(0);
        } else {
            chatId = jdbcTemplate.queryForObject("""
            INSERT INTO app.chat (chat_type, event_id, created_at)
            VALUES ('PRIVATE', NULL, NOW())
            RETURNING chat_id
        """, Long.class);

            jdbcTemplate.update("""
            INSERT INTO app.chat_participant (chat_id, user_id, joined_at)
            VALUES (?, ?, NOW()), (?, ?, NOW())
        """, chatId, userId, chatId, targetUserId);
        }

        return getChatById(chatId, userId);
    }

    private ChatDto getChatById(Long chatId, Long currentUserId) {
        return jdbcTemplate.queryForObject("""
        SELECT c.chat_id,
               COALESCE(
                   e.title,
                   (
                       SELECT CONCAT(p.first_name, ' ', p.last_name)
                       FROM app.chat_participant cp
                       JOIN app.profile p ON p.user_id = cp.user_id
                       WHERE cp.chat_id = c.chat_id
                         AND cp.user_id <> ?
                       LIMIT 1
                   ),
                   'Чат #' || c.chat_id
               ) AS name,
               COALESCE(last_message.message_text, '') AS last_message
        FROM app.chat c
        LEFT JOIN app.event e ON e.event_id = c.event_id
        LEFT JOIN LATERAL (
            SELECT m.message_text
            FROM app.message m
            WHERE m.chat_id = c.chat_id AND m.deleted_at IS NULL
            ORDER BY m.created_at DESC
            LIMIT 1
        ) last_message ON TRUE
        WHERE c.chat_id = ?
    """, (rs, rowNum) -> {
            String name = rs.getString("name");
            String avatarInitial = name == null || name.isBlank()
                    ? "?"
                    : name.substring(0, 1).toUpperCase();

            return new ChatDto(
                    rs.getLong("chat_id"),
                    name,
                    avatarInitial,
                    "онлайн",
                    rs.getString("last_message")
            );
        }, currentUserId, chatId);
    }
}
