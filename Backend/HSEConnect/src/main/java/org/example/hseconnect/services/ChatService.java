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
}
