package com.aiagent.orchestrator.learning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionLearningSignalServiceTest {
    
    private final ExecutionLearningSignalService service = new ExecutionLearningSignalService();
    
    @Test
    void successExecutionReturnsImproved() {
        LearningSignal signal = service.generateSignal("exec-1", true, "PATCH_APPLY", "EXECUTING", 0.8, false);
        
        assertEquals("IMPROVED", signal.getOutcome());
        assertTrue(signal.isSuccess());
    }
    
    @Test
    void retryExecutionReturnsDegraded() {
        LearningSignal signal = service.generateSignal("exec-1", true, "FILE_READ", "EXECUTING", 0.6, true);
        
        assertEquals("DEGRADED", signal.getOutcome());
    }
    
    @Test
    void failedExecutionReturnsFailed() {
        LearningSignal signal = service.generateSignal("exec-1", false, "PATCH_APPLY", "FAILED", 0.0, false);
        
        assertEquals("FAILED", signal.getOutcome());
        assertFalse(signal.isSuccess());
    }
}
