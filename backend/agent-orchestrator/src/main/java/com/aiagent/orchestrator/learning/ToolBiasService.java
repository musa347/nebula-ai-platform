package com.aiagent.orchestrator.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolBiasService {
    private static final Logger log = LoggerFactory.getLogger(ToolBiasService.class);
    
    private static final double SUCCESS_BOOST = 0.1;
    private static final double FAILURE_PENALTY = 0.15;
    private static final double CONTEXT_BONUS = 0.05;
    
    private final LearningSignalStore signalStore;
    
    public ToolBiasService(LearningSignalStore signalStore) {
        this.signalStore = signalStore;
    }
    
    public double adjustScore(String tool, double baseScore) {
        List<LearningSignal> toolHistory = signalStore.filterByTool(tool);
        
        if (toolHistory.isEmpty()) {
            return baseScore;
        }
        
        long successCount = toolHistory.stream()
                .filter(LearningSignal::isSuccess)
                .count();
        
        long failureCount = toolHistory.stream()
                .filter(signal -> !signal.isSuccess())
                .count();
        
        long improvedCount = toolHistory.stream()
                .filter(signal -> "IMPROVED".equals(signal.getOutcome()))
                .count();
        
        double adjustment = 0.0;
        
        // Success boost
        if (successCount > 0) {
            adjustment += SUCCESS_BOOST * (successCount / (double) toolHistory.size());
        }
        
        // Failure penalty
        if (failureCount > 0) {
            adjustment -= FAILURE_PENALTY * (failureCount / (double) toolHistory.size());
        }
        
        // Context bonus for improved outcomes
        if (improvedCount > 0) {
            adjustment += CONTEXT_BONUS * (improvedCount / (double) toolHistory.size());
        }
        
        double finalScore = baseScore + adjustment;
        log.debug("Tool bias adjustment: tool={}, base={}, adjustment={}, final={}", 
                  tool, baseScore, adjustment, finalScore);
        
        return Math.max(0.0, Math.min(1.0, finalScore));
    }
}
