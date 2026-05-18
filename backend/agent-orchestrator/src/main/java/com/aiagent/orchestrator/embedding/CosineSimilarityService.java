package com.aiagent.orchestrator.embedding;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CosineSimilarityService {
    
    public double calculate(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }
        
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += vectorA.get(i) * vectorA.get(i);
            normB += vectorB.get(i) * vectorB.get(i);
        }
        
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (normA * normB);
    }
}
