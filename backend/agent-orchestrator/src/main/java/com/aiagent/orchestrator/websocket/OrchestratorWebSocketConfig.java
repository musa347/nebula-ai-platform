package com.aiagent.orchestrator.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class OrchestratorWebSocketConfig implements WebSocketConfigurer {

    private final OrchestratorWebSocketHandler handler;

    public OrchestratorWebSocketConfig(OrchestratorWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/stream-response").setAllowedOrigins("*");
    }
}
