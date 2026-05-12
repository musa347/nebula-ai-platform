package com.aiagent.orchestrator.websocket;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.orchestrator.retry.RetryOrchestrator;
import com.aiagent.orchestrator.retry.RetryPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrchestratorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorWebSocketHandler.class);

    private final RetryOrchestrator retryOrchestrator;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public OrchestratorWebSocketHandler(RetryOrchestrator retryOrchestrator, ObjectMapper objectMapper) {
        this.retryOrchestrator = retryOrchestrator;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("Plugin connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("Plugin disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String task = node.path("task").asText();
            int maxRetries = node.path("maxRetries").asInt(3);

            if (task.isBlank()) {
                sendText(session, "{\"error\":\"task is required\"}");
                return;
            }

            RetryPolicy policy = new RetryPolicy.Builder().maxRetries(maxRetries).build();

            retryOrchestrator.executeWithRetry(task, policy)
                    .subscribe(
                            event -> sendEvent(session, event),
                            err -> sendText(session, "{\"error\":\"" + err.getMessage() + "\"}"),
                            () -> sendText(session, "{\"type\":\"DONE\"}")
                    );

        } catch (Exception e) {
            log.error("Failed to handle message from {}", session.getId(), e);
            sendText(session, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendEvent(WebSocketSession session, ExecutionEvent event) {
        try {
            sendText(session, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to serialize event", e);
        }
    }

    private void sendText(WebSocketSession session, String text) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(text));
        } catch (IOException e) {
            log.error("Failed to send to session {}", session.getId(), e);
        }
    }
}
