package com.aiagent.orchestrator.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingStoreServiceTest {
    
    private EmbeddingStoreService service;
    
    @BeforeEach
    void setUp() {
        service = new EmbeddingStoreService();
    }
    
    @Test
    void storeAndRetrieve() {
        EmbeddingVector vector = new EmbeddingVector(
            "test-id",
            List.of(0.1f, 0.2f, 0.3f),
            "FILE"
        );
        
        service.save(vector);
        EmbeddingVector retrieved = service.get("test-id");
        
        assertNotNull(retrieved);
        assertEquals("test-id", retrieved.getId());
        assertEquals(3, retrieved.getValues().size());
        assertEquals("FILE", retrieved.getSourceType());
    }
    
    @Test
    void overwriteBehavior() {
        EmbeddingVector vector1 = new EmbeddingVector(
            "test-id",
            List.of(0.1f, 0.2f),
            "FILE"
        );
        
        EmbeddingVector vector2 = new EmbeddingVector(
            "test-id",
            List.of(0.5f, 0.6f, 0.7f),
            "METHOD"
        );
        
        service.save(vector1);
        service.save(vector2);
        
        EmbeddingVector retrieved = service.get("test-id");
        
        assertEquals(3, retrieved.getValues().size());
        assertEquals("METHOD", retrieved.getSourceType());
        assertEquals(1, service.size());
    }
    
    @Test
    void emptyStoreHandling() {
        EmbeddingVector retrieved = service.get("non-existent");
        
        assertNull(retrieved);
        assertEquals(0, service.size());
        assertTrue(service.findAll().isEmpty());
    }
    
    @Test
    void findAllReturnsAllEmbeddings() {
        service.save(new EmbeddingVector("id1", List.of(0.1f), "FILE"));
        service.save(new EmbeddingVector("id2", List.of(0.2f), "METHOD"));
        service.save(new EmbeddingVector("id3", List.of(0.3f), "CLASS"));
        
        List<EmbeddingVector> all = service.findAll();
        
        assertEquals(3, all.size());
    }
}
