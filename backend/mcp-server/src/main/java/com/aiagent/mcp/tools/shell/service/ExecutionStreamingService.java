package com.aiagent.mcp.tools.shell.service;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.mcp.tools.shell.coordinator.ExecutionCoordinator;
import com.aiagent.mcp.tools.shell.websocket.ExecutionWebSocketHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service that bridges execution coordination with WebSocket broadcasting.
 * Provides the main interface for real-time shell command execution with streaming.
 */
@Service
public class ExecutionStreamingService {
    
    private final ExecutionCoordinator executionCoordinator;
    private final ExecutionWebSocketHandler webSocketHandler;
    
    public ExecutionStreamingService(ExecutionCoordinator executionCoordinator, 
                                   ExecutionWebSocketHandler webSocketHandler) {
        this.executionCoordinator = executionCoordinator;
        this.webSocketHandler = webSocketHandler;
    }

    public Flux<ExecutionEvent> executeWithLiveStreaming(String command, String workingDirectory) {
        return executionCoordinator.executeShellCommand(command, workingDirectory)
                .doOnNext(this::broadcastEvent);
    }

    private void broadcastEvent(ExecutionEvent event) {
        webSocketHandler.broadcastEvent(event);
    }
    

    public boolean cancelExecution(String executionId) {
        return executionCoordinator.cancelExecution(executionId);
    }
    

    public Flux<ExecutionEvent> getEventStream(String executionId) {
        return executionCoordinator.getEventStream(executionId);
    }

    public int getRunningExecutionCount() {
        return executionCoordinator.getRunningExecutionCount();
    }
    

    public int getConnectedClientCount() {
        return webSocketHandler.getNumberOfConnectedClient();
    }
}
