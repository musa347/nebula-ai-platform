package com.aiagent.mcp.tools.shell.websocket;

import com.aiagent.common.dto.ExecutionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket handler for broadcasting real-time execution events to connected clients.
 * Enables IntelliJ terminal and other clients to receive live shell command output.
 */
@Component
public class ExecutionWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public ExecutionWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket client connected: " + session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket client disconnected: " + session.getId());
    }

    public void broadcastEvent(ExecutionEvent event) {
        if (sessions.isEmpty()) {
            return;
        }
        
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            TextMessage message = new TextMessage(eventJson);

            sessions.forEach((sessionId, session) -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    } else {
                        sessions.remove(sessionId);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to send message to session " + sessionId + ": " + e.getMessage());
                    sessions.remove(sessionId);
                }
            });
            
        } catch (Exception e) {
            System.err.println("Failed to serialize execution event: " + e.getMessage());
        }
    }

    public int getNumberOfConnectedClient() {
        return (int) sessions.values().stream().filter(WebSocketSession::isOpen).count();
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages from clients if needed
        // For now, we only broadcast events from server to clients
        String payload = message.getPayload();
        System.out.println("Received message from client " + session.getId() + ": " + payload);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error for session " + session.getId() + ": " + exception.getMessage());
        sessions.remove(session.getId());
    }
}
