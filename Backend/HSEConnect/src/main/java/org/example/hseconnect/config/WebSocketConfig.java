package org.example.hseconnect.config;

import org.example.hseconnect.websocket.ChatWebSocketHandler;
import org.example.hseconnect.websocket.NotificationWebSocketHandler;
import org.example.hseconnect.websocket.TypingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TypingWebSocketHandler typingWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public WebSocketConfig(TypingWebSocketHandler typingWebSocketHandler, ChatWebSocketHandler chatWebSocketHandler, NotificationWebSocketHandler notificationWebSocketHandler) {
        this.typingWebSocketHandler = typingWebSocketHandler;
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(typingWebSocketHandler, "/ws/typing")
                .setAllowedOrigins("*");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .setAllowedOrigins("*");
    }
}