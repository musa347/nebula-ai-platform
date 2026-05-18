package com.aiagent.orchestrator.adaptive;

import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ToolStrategyEngineTest {
    
    private LearningSignalStore store;
    private ToolStrategyEngine engine;
    
    @BeforeEach
    void setUp() {
        store = new LearningSignalStore();
        engine = new ToolStrategyEngine(store);
    }
    
    @Test
    void simpleTaskFastPath() {
        store.save(new LearningSignal("exec-1", true, "PATCH_APPLY", "EXECUTING", 0.9, "IMPROVED"));
        store.save(new LearningSignal("exec-2", true, "FILE_READ", "EXECUTING", 0.8, "IMPROVED"));
        
        ToolStrategy strategy = engine.selectStrategy("simple task", false, 1);
        
        assertEquals(ToolStrategy.FAST_PATH, strategy);
    }
    
    @Test
    void riskyTaskSafePath() {
        store.save(new LearningSignal("exec-1", false, "PATCH_APPLY", "FAILED", 0.0, "FAILED"));
        store.save(new LearningSignal("exec-2", false, "FILE_READ", "FAILED", 0.0, "FAILED"));
        
        ToolStrategy strategy = engine.selectStrategy("risky task", false, 1);
        
        assertEquals(ToolStrategy.SAFE_PATH, strategy);
    }
    
    @Test
    void failureTaskRepairPath() {
        ToolStrategy strategy = engine.selectStrategy("retry task", true, 1);
        
        assertEquals(ToolStrategy.REPAIR_PATH, strategy);
    }
}
