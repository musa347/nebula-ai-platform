package com.aiagent.mcp.tools.shell.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for real-time execution event streaming.
 * Enables clients to connect to /ws/execution for live shell command output.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final ExecutionWebSocketHandler executionWebSocketHandler;
    
    public WebSocketConfig(ExecutionWebSocketHandler executionWebSocketHandler) {
        this.executionWebSocketHandler = executionWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(executionWebSocketHandler, "/ws/execution")
                .setAllowedOrigins("*"); // Configure CORS as needed
    }
}
