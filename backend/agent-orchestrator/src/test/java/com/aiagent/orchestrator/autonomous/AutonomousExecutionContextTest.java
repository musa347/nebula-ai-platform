package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.memory.SemanticMemoryEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutonomousExecutionContextTest {
    
    @Test
    void testContextInitializesCorrectly() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-123", "Test task");
        
        assertEquals("exec-123", context.getExecutionId());
        assertEquals("Test task", context.getTask());
        assertEquals(ExecutionState.CREATED, context.getCurrentState());
        assertNotNull(context.getSignals());
        assertNotNull(context.getMemories());
        assertTrue(context.getSignals().isEmpty());
        assertTrue(context.getMemories().isEmpty());
    }
    
    @Test
    void testSupportsNullSafeFields() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-123", "Test task");
        
        assertNull(context.getPlan());
        assertNull(context.getRisk());
        assertNull(context.getLastToolDecision());
        
        // Should not throw when adding null
        context.addSignal(null);
        context.addMemory(null);
        
        assertTrue(context.getSignals().isEmpty());
        assertTrue(context.getMemories().isEmpty());
    }
    
    @Test
    void testUpdatesStateCorrectly() {
        AutonomousExecutionContext context = new AutonomousExecutionContext("exec-123", "Test task");
        
        context.setCurrentState(ExecutionState.PLANNING);
        assertEquals(ExecutionState.PLANNING, context.getCurrentState());
        
        context.setCurrentState(ExecutionState.EXECUTING);
        assertEquals(ExecutionState.EXECUTING, context.getCurrentState());
    }
}
