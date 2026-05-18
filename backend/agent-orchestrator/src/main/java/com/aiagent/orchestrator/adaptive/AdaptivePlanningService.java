package com.aiagent.orchestrator.adaptive;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import com.aiagent.orchestrator.memory.MemoryMatch;
import com.aiagent.orchestrator.memory.SemanticMemoryQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdaptivePlanningService {
    private static final Logger log = LoggerFactory.getLogger(AdaptivePlanningService.class);
    private static final double SIMILARITY_THRESHOLD = 0.8;
    
    private final SemanticMemoryQueryService memoryQuery;
    private final LearningSignalStore learningStore;
    
    public AdaptivePlanningService(SemanticMemoryQueryService memoryQuery, LearningSignalStore learningStore) {
        this.memoryQuery = memoryQuery;
        this.learningStore = learningStore;
    }
    
    public ExecutionPlan enrichPlan(ExecutionPlan originalPlan, String task) {
        if (originalPlan == null || task == null) {
            return originalPlan;
        }
        
        ExecutionPlan enrichedPlan = new ExecutionPlan();
        enrichedPlan.setSteps(new ArrayList<>(originalPlan.getSteps()));
        
        // Rule 1: Similar task exists
        List<MemoryMatch> similarTasks = memoryQuery.query(task, 1);
        if (!similarTasks.isEmpty() && similarTasks.get(0).getScore() > SIMILARITY_THRESHOLD) {
            log.info("Similar task found with score {}, reusing plan structure", similarTasks.get(0).getScore());
            // Plan structure already good, no changes needed
        }
        
        // Rule 2: Failure history exists
        List<MemoryMatch> failures = memoryQuery.query(task, 3);
        long failureCount = failures.stream()
                .filter(m -> "FAILURE".equals(m.getOutcome()))
                .count();
        
        if (failureCount > 0) {
            log.info("Failure history detected, adding verification step");
            PlanStep verificationStep = new PlanStep(
                enrichedPlan.getSteps().size() + 1,
                "Extra verification due to failure history",
                ToolType.NONE,
                "verification"
            );
            enrichedPlan.getSteps().add(verificationStep);
        }
        
        // Rule 3: Tool failure history
        List<LearningSignal> allSignals = learningStore.getAll();
        boolean hasToolFailures = allSignals.stream()
                .anyMatch(signal -> !signal.isSuccess());
        
        if (hasToolFailures) {
            log.info("Tool failure history detected, plan may need tool replacement");
            // Tool replacement handled by ToolStrategyEngine
        }
        
        log.debug("Plan enrichment complete: original steps={}, enriched steps={}", 
                  originalPlan.getSteps().size(), enrichedPlan.getSteps().size());
        
        return enrichedPlan;
    }
}
