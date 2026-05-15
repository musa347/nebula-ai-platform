package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ReplayEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionReplayServiceTest {
    private ExecutionGraphService graphService;
    private ExecutionReplayService replayService;

    @BeforeEach
    void setUp() {
        graphService = new ExecutionGraphService();
        replayService = new ExecutionReplayService(graphService);
    }

    @Test
    void testEmptyExecution() {
        List<ReplayEvent> events = replayService.replay("non-existent");
        
        assertTrue(events.isEmpty());
    }

    @Test
    void testSingleNode() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "CONTEXT", "UserService loaded");
        
        List<ReplayEvent> events = replayService.replay("exec-001");
        
        assertEquals(1, events.size());
        assertEquals("exec-001", events.get(0).getExecutionId());
        assertEquals("CONTEXT", events.get(0).getType());
        assertEquals("UserService loaded", events.get(0).getMessage());
        assertTrue(events.get(0).getTimestamp() > 0);
    }

    @Test
    void testMultipleNodesCorrectOrder() {
        graphService.create("exec-002");
        
        // Add nodes with small delays to ensure different timestamps
        graphService.addNode("exec-002", "CONTEXT", "First");
        sleep(5);
        graphService.addNode("exec-002", "PATCH", "Second");
        sleep(5);
        graphService.addNode("exec-002", "RETRY", "Third");
        
        List<ReplayEvent> events = replayService.replay("exec-002");
        
        assertEquals(3, events.size());
        assertEquals("CONTEXT", events.get(0).getType());
        assertEquals("PATCH", events.get(1).getType());
        assertEquals("RETRY", events.get(2).getType());
        
        // Verify timestamps are in ascending order
        assertTrue(events.get(0).getTimestamp() < events.get(1).getTimestamp());
        assertTrue(events.get(1).getTimestamp() < events.get(2).getTimestamp());
    }

    @Test
    void testTypePreservation() {
        graphService.create("exec-003");
        graphService.addNode("exec-003", "SYMBOL", "Found class");
        graphService.addNode("exec-003", "DEPENDENCY", "Analyzed deps");
        graphService.addNode("exec-003", "TOOL_CALL", "filesystem.read");
        graphService.addNode("exec-003", "FAILURE", "Error occurred");
        
        List<ReplayEvent> events = replayService.replay("exec-003");
        
        assertEquals(4, events.size());
        assertEquals("SYMBOL", events.get(0).getType());
        assertEquals("DEPENDENCY", events.get(1).getType());
        assertEquals("TOOL_CALL", events.get(2).getType());
        assertEquals("FAILURE", events.get(3).getType());
    }

    @Test
    void testMissingExecutionId() {
        List<ReplayEvent> events = replayService.replay("missing-id");
        
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
