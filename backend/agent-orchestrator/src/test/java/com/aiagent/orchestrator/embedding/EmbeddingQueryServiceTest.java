package com.aiagent.orchestrator.embedding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingQueryServiceTest {
    
    private EmbeddingStoreService storeService;
    private EmbeddingService embeddingService;
    private SemanticSearchService searchService;
    private EmbeddingQueryService queryService;
    
    @BeforeEach
    void setUp() {
        storeService = new EmbeddingStoreService();
        embeddingService = new EmbeddingService("http://localhost:11434", "nomic-embed-text", new ObjectMapper());
        CosineSimilarityService similarityService = new CosineSimilarityService();
        searchService = new SemanticSearchService(storeService, similarityService);
        queryService = new EmbeddingQueryService(embeddingService, searchService);
    }
    
    @Test
    void emptyQueryHandledSafely() {
        List<SemanticMatch> matches = queryService.query("", 5);
        
        assertTrue(matches.isEmpty());
    }
    
    @Test
    void invalidEmbeddingFallbackSafe() {
        List<SemanticMatch> matches = queryService.query("test query", 5);
        
        assertNotNull(matches);
    }
}
