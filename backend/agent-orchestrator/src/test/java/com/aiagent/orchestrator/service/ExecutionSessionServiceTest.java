package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionSession;
import com.aiagent.common.model.TransitionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionSessionServiceTest {

    private ExecutionSessionService executionSessionService;
    private StateTransitionService stateTransitionService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
        executionSessionService = new ExecutionSessionService();
        // Use reflection to inject dependency for test
        try {
            java.lang.reflect.Field field = ExecutionSessionService.class.getDeclaredField("stateTransitionService");
            field.setAccessible(true);
            field.set(executionSessionService, stateTransitionService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateSession_InitialStateIsCreated() {
        ExecutionSession session = executionSessionService.create();
        
        assertNotNull(session.getExecutionId());
        assertEquals(ExecutionState.CREATED, session.getCurrentState());
        assertTrue(session.getCreatedAt() > 0);
        assertEquals(session.getCreatedAt(), session.getUpdatedAt());
    }

    @Test
    void testValidStateUpdate_CreatedToPlanning() {
        ExecutionSession session = executionSessionService.create();
        String executionId = session.getExecutionId();
        long originalUpdatedAt = session.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        
        TransitionResult result = executionSessionService.updateState(executionId, ExecutionState.PLANNING);
        
        assertTrue(result.isAllowed());
        
        ExecutionSession updatedSession = executionSessionService.get(executionId);
        assertEquals(ExecutionState.PLANNING, updatedSession.getCurrentState());
        assertTrue(updatedSession.getUpdatedAt() > originalUpdatedAt);
    }

    @Test
    void testInvalidStateUpdate_CompletedToExecuting() {
        ExecutionSession session = executionSessionService.create();
        String executionId = session.getExecutionId();
        
        // Force session to COMPLETED state
        session.setCurrentState(ExecutionState.COMPLETED);
        
        TransitionResult result = executionSessionService.updateState(executionId, ExecutionState.EXECUTING);
        
        assertFalse(result.isAllowed());
        assertTrue(result.getMessage().contains("Invalid transition"));
        
        ExecutionSession unchangedSession = executionSessionService.get(executionId);
        assertEquals(ExecutionState.COMPLETED, unchangedSession.getCurrentState());
    }

    @Test
    void testTimestampUpdate_UpdatedAtChanges() {
        ExecutionSession session = executionSessionService.create();
        String executionId = session.getExecutionId();
        long originalUpdatedAt = session.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try { Thread.sleep(2); } catch (InterruptedException e) {}
        
        executionSessionService.updateState(executionId, ExecutionState.PLANNING);
        
        ExecutionSession updatedSession = executionSessionService.get(executionId);
        assertTrue(updatedSession.getUpdatedAt() > originalUpdatedAt);
    }

    @Test
    void testMissingSession_SafeHandling() {
        TransitionResult result = executionSessionService.updateState("nonexistent", ExecutionState.PLANNING);
        
        assertFalse(result.isAllowed());
        assertEquals("Session not found", result.getMessage());
        
        ExecutionSession session = executionSessionService.get("nonexistent");
        assertNull(session);
    }
}