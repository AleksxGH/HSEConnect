package org.example.hseconnect.config;

import org.example.hseconnect.websocket.TypingWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TypingWebSocketHandler typingWebSocketHandler;

    public WebSocketConfig(TypingWebSocketHandler typingWebSocketHandler) {
        this.typingWebSocketHandler = typingWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(typingWebSocketHandler, "/ws/typing")
                .setAllowedOrigins("*");
    }
}