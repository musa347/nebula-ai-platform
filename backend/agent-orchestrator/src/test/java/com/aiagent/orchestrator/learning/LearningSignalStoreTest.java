package com.aiagent.orchestrator.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LearningSignalStoreTest {
    
    private LearningSignalStore store;
    
    @BeforeEach
    void setUp() {
        store = new LearningSignalStore();
    }
    
    @Test
    void saveAndRetrieveWorks() {
        LearningSignal signal = new LearningSignal("exec-1", true, "PATCH_APPLY", "EXECUTING", 0.8, "IMPROVED");
        
        store.save(signal);
        LearningSignal retrieved = store.get("exec-1");
        
        assertNotNull(retrieved);
        assertEquals("exec-1", retrieved.getExecutionId());
        assertEquals("IMPROVED", retrieved.getOutcome());
    }
    
    @Test
    void filterWorks() {
        store.save(new LearningSignal("exec-1", true, "PATCH_APPLY", "EXECUTING", 0.8, "IMPROVED"));
        store.save(new LearningSignal("exec-2", true, "FILE_READ", "EXECUTING", 0.7, "IMPROVED"));
        store.save(new LearningSignal("exec-3", true, "PATCH_APPLY", "EXECUTING", 0.9, "IMPROVED"));
        
        List<LearningSignal> filtered = store.filterByTool("PATCH_APPLY");
        
        assertEquals(2, filtered.size());
    }
    
    @Test
    void emptyStoreSafe() {
        assertNull(store.get("non-existent"));
        assertTrue(store.getAll().isEmpty());
        assertTrue(store.filterByTool("PATCH_APPLY").isEmpty());
    }
}
