package com.aiagent.orchestrator.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticSearchServiceTest {
    
    private EmbeddingStoreService storeService;
    private CosineSimilarityService similarityService;
    private SemanticSearchService searchService;
    
    @BeforeEach
    void setUp() {
        storeService = new EmbeddingStoreService();
        similarityService = new CosineSimilarityService();
        searchService = new SemanticSearchService(storeService, similarityService);
    }
    
    @Test
    void exactMatchReturnsTopScore() {
        List<Float> embedding = List.of(1.0f, 2.0f, 3.0f);
        storeService.save(new EmbeddingVector("test-id", embedding, "FILE"));
        
        List<SemanticMatch> matches = searchService.search(embedding, 5);
        
        assertEquals(1, matches.size());
        assertEquals("test-id", matches.get(0).getId());
        assertEquals(1.0, matches.get(0).getScore(), 0.0001);
    }
    
    @Test
    void multipleMatchesSortedCorrectly() {
        storeService.save(new EmbeddingVector("id1", List.of(1.0f, 0.0f, 0.0f), "FILE"));
        storeService.save(new EmbeddingVector("id2", List.of(0.9f, 0.1f, 0.0f), "FILE"));
        storeService.save(new EmbeddingVector("id3", List.of(0.0f, 1.0f, 0.0f), "FILE"));
        
        List<SemanticMatch> matches = searchService.search(List.of(1.0f, 0.0f, 0.0f), 5);
        
        assertEquals(3, matches.size());
        assertEquals("id1", matches.get(0).getId());
        assertTrue(matches.get(0).getScore() > matches.get(1).getScore());
        assertTrue(matches.get(1).getScore() > matches.get(2).getScore());
    }
    
    @Test
    void emptyStoreReturnsEmptyList() {
        List<SemanticMatch> matches = searchService.search(List.of(1.0f, 2.0f), 5);
        
        assertTrue(matches.isEmpty());
    }
    
    @Test
    void kLimitEnforced() {
        for (int i = 0; i < 10; i++) {
            storeService.save(new EmbeddingVector("id" + i, List.of(1.0f, 2.0f), "FILE"));
        }
        
        List<SemanticMatch> matches = searchService.search(List.of(1.0f, 2.0f), 3);
        
        assertEquals(3, matches.size());
    }
}
