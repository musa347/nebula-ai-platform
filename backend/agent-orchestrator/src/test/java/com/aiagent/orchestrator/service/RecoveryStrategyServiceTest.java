package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiFailureResponse;
import com.aiagent.orchestrator.model.RecoveryStrategy;
import com.aiagent.orchestrator.model.RecoveryAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryStrategyServiceTest {
    
    private final RecoveryStrategyService service = new RecoveryStrategyService();
    
    @Test
    void compileFailureStrategy() {
        AiFailureResponse analysis = new AiFailureResponse();
        analysis.setFailureType("COMPILATION_ERROR");
        analysis.setRootCause("Syntax error");
        analysis.setSuggestedRecovery("Fix syntax");
        
        RecoveryStrategy strategy = service.buildStrategy(analysis);
        
        assertEquals("COMPILATION_ERROR", strategy.getFailureType());
        assertEquals(2, strategy.getMaxRetries());
        assertTrue(strategy.getActions().contains(RecoveryAction.REGENERATE_PATCH));
        assertTrue(strategy.getActions().contains(RecoveryAction.RETRY_EXECUTION));
    }
    
    @Test
    void timeoutStrategy() {
        AiFailureResponse analysis = new AiFailureResponse();
        analysis.setFailureType("TIMEOUT_ERROR");
        analysis.setRootCause("Execution timeout");
        analysis.setSuggestedRecovery("Reduce scope");
        
        RecoveryStrategy strategy = service.buildStrategy(analysis);
        
        assertEquals(1, strategy.getMaxRetries());
        assertTrue(strategy.getActions().contains(RecoveryAction.REDUCE_SCOPE));
    }
    
    @Test
    void syntaxFailureStrategy() {
        AiFailureResponse analysis = new AiFailureResponse();
        analysis.setFailureType("SYNTAX_ERROR");
        analysis.setRootCause("Invalid syntax");
        analysis.setSuggestedRecovery("Regenerate");
        
        RecoveryStrategy strategy = service.buildStrategy(analysis);
        
        assertTrue(strategy.getActions().contains(RecoveryAction.REGENERATE_PATCH));
    }
    
    @Test
    void unknownFailureSafeFallback() {
        AiFailureResponse analysis = new AiFailureResponse();
        analysis.setFailureType("UNKNOWN_ERROR");
        analysis.setRootCause("Unknown");
        analysis.setSuggestedRecovery("Retry");
        
        RecoveryStrategy strategy = service.buildStrategy(analysis);
        
        assertNotNull(strategy.getActions());
        assertFalse(strategy.getActions().isEmpty());
        assertTrue(strategy.getActions().contains(RecoveryAction.REGENERATE_PATCH));
    }
}
