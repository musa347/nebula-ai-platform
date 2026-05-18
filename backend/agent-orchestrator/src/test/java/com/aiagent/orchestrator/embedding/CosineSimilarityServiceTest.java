package com.aiagent.orchestrator.embedding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CosineSimilarityServiceTest {
    
    private final CosineSimilarityService service = new CosineSimilarityService();
    
    @Test
    void identicalVectorsReturnOne() {
        List<Float> vector = List.of(1.0f, 2.0f, 3.0f);
        
        double similarity = service.calculate(vector, vector);
        
        assertEquals(1.0, similarity, 0.0001);
    }
    
    @Test
    void oppositeVectorsReturnZero() {
        List<Float> vectorA = List.of(1.0f, 0.0f, 0.0f);
        List<Float> vectorB = List.of(0.0f, 1.0f, 0.0f);
        
        double similarity = service.calculate(vectorA, vectorB);
        
        assertEquals(0.0, similarity, 0.0001);
    }
    
    @Test
    void emptyVectorsSafeFallback() {
        double similarity = service.calculate(List.of(), List.of());
        
        assertEquals(0.0, similarity);
    }
    
    @Test
    void nullSafety() {
        double similarity1 = service.calculate(null, List.of(1.0f));
        double similarity2 = service.calculate(List.of(1.0f), null);
        
        assertEquals(0.0, similarity1);
        assertEquals(0.0, similarity2);
    }
}
