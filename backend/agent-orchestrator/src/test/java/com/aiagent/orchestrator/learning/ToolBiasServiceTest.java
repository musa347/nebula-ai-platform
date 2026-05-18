package com.aiagent.orchestrator.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolBiasServiceTest {
    
    private LearningSignalStore store;
    private ToolBiasService service;
    
    @BeforeEach
    void setUp() {
        store = new LearningSignalStore();
        service = new ToolBiasService(store);
    }
    
    @Test
    void successfulToolHigherRanking() {
        store.save(new LearningSignal("exec-1", true, "PATCH_APPLY", "EXECUTING", 0.9, "IMPROVED"));
        store.save(new LearningSignal("exec-2", true, "PATCH_APPLY", "EXECUTING", 0.8, "IMPROVED"));
        
        double adjustedScore = service.adjustScore("PATCH_APPLY", 0.5);
        
        assertTrue(adjustedScore > 0.5);
    }
    
    @Test
    void failedToolLowerRanking() {
        store.save(new LearningSignal("exec-1", false, "FILE_READ", "FAILED", 0.0, "FAILED"));
        store.save(new LearningSignal("exec-2", false, "FILE_READ", "FAILED", 0.0, "FAILED"));
        
        double adjustedScore = service.adjustScore("FILE_READ", 0.5);
        
        assertTrue(adjustedScore < 0.5);
    }
    
    @Test
    void neutralToolUnchanged() {
        double adjustedScore = service.adjustScore("UNKNOWN_TOOL", 0.5);
        
        assertEquals(0.5, adjustedScore, 0.001);
    }
}
