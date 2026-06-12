package org.example.hseconnect.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> sessionsByUserId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getLongParam(session, "userId");

        if (userId != null) {
            sessionsByUserId.put(userId, session);
        }
    }

    public void sendNewNotificationToUser(Long userId) {
        sendToUser(userId, "new_notification");
    }

    public void sendNotificationReadToUser(Long userId) {
        sendToUser(userId, "notification_read");
    }

    private void sendToUser(Long userId, String type) {
        WebSocketSession session = sessionsByUserId.get(userId);

        if (session == null || !session.isOpen()) return;

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", type
            ));

            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionsByUserId.entrySet().removeIf(entry ->
                entry.getValue().getId().equals(session.getId())
        );
    }

    private Long getLongParam(WebSocketSession session, String name) {
        String query = session.getUri().getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] parts = param.split("=");

            if (parts.length == 2 && parts[0].equals(name)) {
                return Long.parseLong(parts[1]);
            }
        }

        return null;
    }
}