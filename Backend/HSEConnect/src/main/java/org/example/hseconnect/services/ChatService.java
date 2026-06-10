package org.example.hseconnect.services;

import org.example.hseconnect.model.ChatDto;
import org.example.hseconnect.model.MessageDto;
import org.example.hseconnect.websocket.ChatWebSocketHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class ChatService {

    private final JdbcTemplate jdbcTemplate;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatService(JdbcTemplate jdbcTemplate, ChatWebSocketHandler chatWebSocketHandler) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    public List<ChatDto> getChats(Long currentUserId) {
        return jdbcTemplate.query("""
        SELECT c.chat_id,
               COALESCE(
                   e.title,
                   (
                       SELECT CONCAT(p.first_name, ' ', p.last_name)
                       FROM app.chat_participant cp_other
                       JOIN app.profile p ON p.user_id = cp_other.user_id
                       WHERE cp_other.chat_id = c.chat_id
                         AND cp_other.user_id <> ?
                         AND cp_other.left_at IS NULL
                       LIMIT 1
                   ),
                   'Чат #' || c.chat_id
               ) AS name,
               COALESCE(last_message.message_text, '') AS last_message,
            (
                SELECT COUNT(*)
                FROM app.message m_unread
                WHERE m_unread.chat_id = c.chat_id
                  AND m_unread.sender_id <> ?
                  AND m_unread.deleted_at IS NULL
                  AND NOT EXISTS (
                      SELECT 1
                      FROM app.message_read mr
                      WHERE mr.message_id = m_unread.message_id
                        AND mr.user_id = ?
                  )
            ) AS unread_count
        FROM app.chat c
        JOIN app.chat_participant cp_me ON cp_me.chat_id = c.chat_id
        LEFT JOIN app.event e ON e.event_id = c.event_id
        LEFT JOIN LATERAL (
                                  SELECT m.message_text, m.created_at
                                  FROM app.message m
                                  WHERE m.chat_id = c.chat_id AND m.deleted_at IS NULL
                                  ORDER BY m.created_at DESC
                                  LIMIT 1
                              ) last_message ON TRUE
        WHERE cp_me.user_id = ?
          AND cp_me.left_at IS NULL
        ORDER BY COALESCE(last_message.created_at, c.created_at) DESC
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
                    rs.getString("last_message"),
                    rs.getInt("unread_count")
            );
        }, currentUserId, currentUserId, currentUserId, currentUserId);
    }

    @Transactional
    public void markChatAsRead(Long chatId, Long currentUserId) {
        validateChatExists(chatId);
        validateUserInChat(chatId, currentUserId);

        jdbcTemplate.update("""
        INSERT INTO app.message_read (message_id, user_id, read_at)
        SELECT m.message_id, ?, NOW()
        FROM app.message m
        WHERE m.chat_id = ?
          AND m.sender_id <> ?
          AND m.deleted_at IS NULL
          AND NOT EXISTS (
              SELECT 1
              FROM app.message_read mr
              WHERE mr.message_id = m.message_id
                AND mr.user_id = ?
          )
    """, currentUserId, chatId, currentUserId, currentUserId);
    }

    public List<MessageDto> getMessages(Long chatId, Long currentUserId) {
        validateChatExists(chatId);
        validateUserInChat(chatId, currentUserId);

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
                    : createdAt.toLocalDateTime()
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));

            MessageDto dto = new MessageDto(
                    rs.getLong("message_id"),
                    rs.getLong("chat_id"),
                    rs.getString("message_text"),
                    currentUserId.equals(senderId) ? "outgoing" : "incoming",
                    time
            );

            dto.setSenderId(senderId);

            if (createdAt != null) {
                dto.setCreatedAt(
                        createdAt.toLocalDateTime()
                                .atZone(ZoneId.of("Europe/Moscow"))
                                .toInstant()
                                .toString()
                );
            }

            return dto;
        }, chatId);
    }

    @Transactional
    public MessageDto sendMessage(Long chatId, Long currentUserId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Сообщение не может быть пустым");
        }

        validateChatExists(chatId);
        validateUserInChat(chatId, currentUserId);

        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        ZonedDateTime nowMoscow = ZonedDateTime.now(moscowZone);
        LocalDateTime now = nowMoscow.toLocalDateTime();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
            INSERT INTO app.message 
            (chat_id, sender_id, message_text, created_at, edited_at, deleted_at)
            VALUES (?, ?, ?, ?, NULL, NULL)
        """, new String[]{"message_id"});

            ps.setLong(1, chatId);
            ps.setLong(2, currentUserId);
            ps.setString(3, text.trim());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        MessageDto dto = new MessageDto(
                Objects.requireNonNull(keyHolder.getKey()).longValue(),
                chatId,
                text.trim(),
                "outgoing",
                now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        dto.setSenderId(currentUserId);
        dto.setCreatedAt(nowMoscow.toInstant().toString());

        List<Long> receiverIds = jdbcTemplate.queryForList("""
    SELECT user_id
    FROM app.chat_participant
    WHERE chat_id = ?
      AND user_id <> ?
      AND left_at IS NULL
""", Long.class, chatId, currentUserId);

        for (Long receiverId : receiverIds) {
            MessageDto incomingDto = new MessageDto(
                    dto.getId(),
                    dto.getChatId(),
                    dto.getText(),
                    "incoming",
                    dto.getTime()
            );

            incomingDto.setSenderId(currentUserId);
            incomingDto.setCreatedAt(dto.getCreatedAt());

            chatWebSocketHandler.sendMessageToUser(receiverId, incomingDto);
        }

        return dto;
    }

    private void validateUserInChat(Long chatId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM app.chat_participant
        WHERE chat_id = ?
          AND user_id = ?
          AND left_at IS NULL
    """, Integer.class, chatId, userId);

        if (count == null || count == 0) {
            throw new RuntimeException("У пользователя нет доступа к этому чату");
        }
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
