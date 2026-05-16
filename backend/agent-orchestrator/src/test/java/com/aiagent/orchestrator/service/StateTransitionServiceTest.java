package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.TransitionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionServiceTest {

    private StateTransitionService stateTransitionService;

    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService();
    }

    @Test
    void testValidTransition_CreatedToPlanning() {
        TransitionResult result = stateTransitionService.canTransition(
            ExecutionState.CREATED, 
            ExecutionState.PLANNING
        );
        
        assertTrue(result.isAllowed());
        assertEquals("Valid transition", result.getMessage());
    }

    @Test
    void testInvalidTransition_CompletedToExecuting() {
        TransitionResult result = stateTransitionService.canTransition(
            ExecutionState.COMPLETED, 
            ExecutionState.EXECUTING
        );
        
        assertFalse(result.isAllowed());
        assertTrue(result.getMessage().contains("Invalid transition"));
    }

    @Test
    void testRetryFlow_VerifyingToRetrying() {
        TransitionResult result = stateTransitionService.canTransition(
            ExecutionState.VERIFYING, 
            ExecutionState.RETRYING
        );
        
        assertTrue(result.isAllowed());
        assertEquals("Valid transition", result.getMessage());
    }

    @Test
    void testFailureTransition_ExecutingToFailed() {
        TransitionResult result = stateTransitionService.canTransition(
            ExecutionState.EXECUTING, 
            ExecutionState.FAILED
        );
        
        assertTrue(result.isAllowed());
        assertTrue(result.getMessage().contains("FAILED allowed"));
    }

    @Test
    void testNullSafety_HandlesNullStates() {
        TransitionResult result = stateTransitionService.canTransition(null, ExecutionState.PLANNING);
        
        assertFalse(result.isAllowed());
        assertEquals("States cannot be null", result.getMessage());
        
        result = stateTransitionService.canTransition(ExecutionState.CREATED, null);
        
        assertFalse(result.isAllowed());
        assertEquals("States cannot be null", result.getMessage());
    }
}