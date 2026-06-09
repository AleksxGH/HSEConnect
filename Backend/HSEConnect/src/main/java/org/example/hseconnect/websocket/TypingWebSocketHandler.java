package org.example.hseconnect.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TypingWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, WebSocketSession> sessionsByUserId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getLongParam(session, "userId");

        if (userId != null) {
            sessionsByUserId.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = objectMapper.readTree(message.getPayload());

        String type = json.get("type").asText();
        Long chatId = json.get("chatId").asLong();
        Long fromUserId = json.get("fromUserId").asLong();
        Long toUserId = json.get("toUserId").asLong();

        WebSocketSession receiverSession = sessionsByUserId.get(toUserId);

        if (receiverSession != null && receiverSession.isOpen()) {
            String response = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "chatId", chatId,
                    "fromUserId", fromUserId
            ));

            receiverSession.sendMessage(new TextMessage(response));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionsByUserId.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
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