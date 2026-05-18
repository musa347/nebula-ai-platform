package com.aiagent.orchestrator.memory;

import com.aiagent.orchestrator.embedding.CosineSimilarityService;
import com.aiagent.orchestrator.embedding.EmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticMemoryQueryServiceTest {
    
    private SemanticMemoryStore store;
    private SemanticMemoryQueryService queryService;
    
    @BeforeEach
    void setUp() {
        store = new SemanticMemoryStore();
        EmbeddingService embeddingService = new EmbeddingService("http://localhost:11434", "nomic-embed-text", new ObjectMapper());
        CosineSimilarityService similarityService = new CosineSimilarityService();
        queryService = new SemanticMemoryQueryService(embeddingService, store, similarityService);
    }
    
    @Test
    void emptyStoreSafe() {
        List<MemoryMatch> matches = queryService.query("test query", 5);
        
        assertTrue(matches.isEmpty());
    }
    
    @Test
    void topKEnforced() {
        for (int i = 0; i < 10; i++) {
            SemanticMemoryEntry entry = new SemanticMemoryEntry();
            entry.setId("id" + i);
            entry.setEmbedding(List.of(1.0f, 2.0f));
            store.save(entry);
        }
        
        List<MemoryMatch> matches = queryService.query("test", 3);
        
        assertTrue(matches.size() <= 3);
    }
}
