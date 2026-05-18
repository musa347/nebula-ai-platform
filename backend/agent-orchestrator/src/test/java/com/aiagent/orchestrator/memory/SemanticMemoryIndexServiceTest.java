package com.aiagent.orchestrator.memory;

import com.aiagent.orchestrator.embedding.EmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticMemoryIndexServiceTest {
    
    private SemanticMemoryStore store;
    private EmbeddingService embeddingService;
    private SemanticMemoryIndexService indexService;
    
    @BeforeEach
    void setUp() {
        store = new SemanticMemoryStore();
        embeddingService = new EmbeddingService("http://localhost:11434", "nomic-embed-text", new ObjectMapper());
        indexService = new SemanticMemoryIndexService(embeddingService, store);
    }
    
    @Test
    void storeUpdatesCorrectly() {
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId("test-id");
        entry.setExecutionId("exec-1");
        entry.setSummary("test summary");
        
        indexService.indexMemory(entry);
        
        SemanticMemoryEntry stored = store.get("test-id");
        assertNotNull(stored);
        assertEquals("test-id", stored.getId());
    }
    
    @Test
    void nullSafeBehavior() {
        SemanticMemoryEntry entry = new SemanticMemoryEntry();
        entry.setId("test-id");
        entry.setSummary(null);
        
        assertDoesNotThrow(() -> indexService.indexMemory(entry));
    }
}
