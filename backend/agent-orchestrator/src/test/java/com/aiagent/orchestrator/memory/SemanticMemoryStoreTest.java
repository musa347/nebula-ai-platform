package com.aiagent.orchestrator.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticMemoryStoreTest {
    
    private SemanticMemoryStore store;
    
    @BeforeEach
    void setUp() {
        store = new SemanticMemoryStore();
    }
    
    @Test
    void saveAndRetrieve() {
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId("test-id");
        entry.setExecutionId("exec-1");
        entry.setTask("test task");
        entry.setOutcome("SUCCESS");
        
        store.save(entry);
        SemanticMemoryEntry retrieved = store.get("test-id");
        
        assertNotNull(retrieved);
        assertEquals("test-id", retrieved.getId());
        assertEquals("exec-1", retrieved.getExecutionId());
    }
    
    @Test
    void overwriteBehavior() {
        SemanticMemoryEntry entry1 = new SemanticMemoryEntry();
        entry1.setId("test-id");
        entry1.setOutcome("SUCCESS");
        
        SemanticMemoryEntry entry2 = new SemanticMemoryEntry();
        entry2.setId("test-id");
        entry2.setOutcome("FAILURE");
        
        store.save(entry1);
        store.save(entry2);
        
        assertEquals("FAILURE", store.get("test-id").getOutcome());
        assertEquals(1, store.size());
    }
    
    @Test
    void queryByExecutionId() {
        SemanticMemoryEntry entry1 = new SemanticMemoryEntry();
        entry1.setId("id1");
        entry1.setExecutionId("exec-1");
        
        SemanticMemoryEntry entry2 = new SemanticMemoryEntry();
        entry2.setId("id2");
        entry2.setExecutionId("exec-1");
        
        SemanticMemoryEntry entry3 = new SemanticMemoryEntry();
        entry3.setId("id3");
        entry3.setExecutionId("exec-2");
        
        store.save(entry1);
        store.save(entry2);
        store.save(entry3);
        
        List<SemanticMemoryEntry> results = store.getByExecutionId("exec-1");
        
        assertEquals(2, results.size());
    }
    
    @Test
    void emptyStoreSafe() {
        assertNull(store.get("non-existent"));
        assertTrue(store.getByExecutionId("exec-1").isEmpty());
        assertTrue(store.findAll().isEmpty());
    }
}
