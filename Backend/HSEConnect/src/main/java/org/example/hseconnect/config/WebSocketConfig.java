package org.example.hseconnect.config;

import org.example.hseconnect.websocket.ChatWebSocketHandler;
import org.example.hseconnect.websocket.TypingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TypingWebSocketHandler typingWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(TypingWebSocketHandler typingWebSocketHandler, ChatWebSocketHandler chatWebSocketHandler) {
        this.typingWebSocketHandler = typingWebSocketHandler;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(typingWebSocketHandler, "/ws/typing")
                .setAllowedOrigins("*");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}