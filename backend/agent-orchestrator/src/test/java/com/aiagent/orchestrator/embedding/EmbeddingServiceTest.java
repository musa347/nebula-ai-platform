package com.aiagent.orchestrator.embedding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void emptyContentHandled() {
        EmbeddingService service = new EmbeddingService("http://localhost:11434", "nomic-embed-text", objectMapper);
        
        EmbeddingRequest request = new EmbeddingRequest("", "test-id");
        EmbeddingResponse response = service.generateEmbedding(request);
        
        assertEquals("test-id", response.getSourceId());
        assertTrue(response.getEmbedding().isEmpty());
    }
    
    @Test
    void apiFailureFallback() {
        EmbeddingService service = new EmbeddingService("http://invalid:9999", "nomic-embed-text", objectMapper);
        
        EmbeddingRequest request = new EmbeddingRequest("test content", "test-id");
        
        assertThrows(RuntimeException.class, () -> service.generateEmbedding(request));
    }
}
