package com.aiagent.orchestrator.adaptive;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.aiagent.orchestrator.embedding.CosineSimilarityService;
import com.aiagent.orchestrator.embedding.EmbeddingService;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import com.aiagent.orchestrator.memory.SemanticMemoryQueryService;
import com.aiagent.orchestrator.memory.SemanticMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdaptivePlanningServiceTest {
    
    private AdaptivePlanningService service;
    
    @BeforeEach
    void setUp() {
        SemanticMemoryStore memoryStore = new SemanticMemoryStore();
        EmbeddingService embeddingService = new EmbeddingService("http://localhost:11434", "nomic-embed-text", new ObjectMapper());
        CosineSimilarityService similarityService = new CosineSimilarityService();
        SemanticMemoryQueryService memoryQuery = new SemanticMemoryQueryService(embeddingService, memoryStore, similarityService);
        LearningSignalStore learningStore = new LearningSignalStore();
        
        service = new AdaptivePlanningService(memoryQuery, learningStore);
    }
    
    @Test
    void noMemoryOriginalPlanUnchanged() {
        ExecutionPlan original = new ExecutionPlan();
        List<PlanStep> steps = new ArrayList<>();
        steps.add(new PlanStep(1, "Step 1", ToolType.FILE_READ, "target"));
        steps.add(new PlanStep(2, "Step 2", ToolType.PATCH_APPLY, "target"));
        original.setSteps(steps);
        
        ExecutionPlan enriched = service.enrichPlan(original, "test task");
        
        assertNotNull(enriched);
        assertEquals(2, enriched.getSteps().size());
    }
}
