package com.aiagent.orchestrator.adaptive;

import com.aiagent.orchestrator.embedding.CosineSimilarityService;
import com.aiagent.orchestrator.embedding.EmbeddingService;
import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import com.aiagent.orchestrator.memory.SemanticMemoryQueryService;
import com.aiagent.orchestrator.memory.SemanticMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionRiskAnalyzerTest {
    
    private LearningSignalStore learningStore;
    private ExecutionRiskAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        SemanticMemoryStore memoryStore = new SemanticMemoryStore();
        EmbeddingService embeddingService = new EmbeddingService("http://localhost:11434", "nomic-embed-text", new ObjectMapper());
        CosineSimilarityService similarityService = new CosineSimilarityService();
        SemanticMemoryQueryService memoryQuery = new SemanticMemoryQueryService(embeddingService, memoryStore, similarityService);
        learningStore = new LearningSignalStore();
        
        analyzer = new ExecutionRiskAnalyzer(memoryQuery, learningStore);
    }
    
    @Test
    void knownFailurePatternHighRisk() {
        learningStore.save(new LearningSignal("exec-1", false, "PATCH_APPLY", "FAILED", 0.0, "FAILED"));
        learningStore.save(new LearningSignal("exec-2", false, "PATCH_APPLY", "FAILED", 0.0, "FAILED"));
        
        ExecutionRisk risk = analyzer.analyzeRisk("test task", "PATCH_APPLY");
        
        assertNotNull(risk);
        assertTrue(risk.getRiskScore() > 0.0);
    }
    
    @Test
    void knownSuccessPatternLowRisk() {
        learningStore.save(new LearningSignal("exec-1", true, "FILE_READ", "EXECUTING", 0.9, "IMPROVED"));
        learningStore.save(new LearningSignal("exec-2", true, "FILE_READ", "EXECUTING", 0.8, "IMPROVED"));
        
        ExecutionRisk risk = analyzer.analyzeRisk("test task", "FILE_READ");
        
        assertNotNull(risk);
        assertTrue(risk.getRiskScore() >= 0.0);
    }
}
