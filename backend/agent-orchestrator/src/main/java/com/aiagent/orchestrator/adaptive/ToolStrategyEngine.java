package com.aiagent.orchestrator.adaptive;

import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolStrategyEngine {
    private static final Logger log = LoggerFactory.getLogger(ToolStrategyEngine.class);
    
    private final LearningSignalStore learningStore;
    
    public ToolStrategyEngine(LearningSignalStore learningStore) {
        this.learningStore = learningStore;
    }
    
    public ToolStrategy selectStrategy(String task, boolean hasRetries, int fileCount) {
        List<LearningSignal> allSignals = learningStore.getAll();
        
        // Calculate success rate
        long totalExecutions = allSignals.size();
        long successCount = allSignals.stream().filter(LearningSignal::isSuccess).count();
        double successRate = totalExecutions > 0 ? (double) successCount / totalExecutions : 0.5;
        
        // Rule: Previous retry → REPAIR_PATH
        if (hasRetries) {
            log.info("Retry detected, selecting REPAIR_PATH strategy");
            return ToolStrategy.REPAIR_PATH;
        }
        
        // Rule: Multi-file complexity → DEEP_ANALYSIS_PATH
        if (fileCount > 3) {
            log.info("Multi-file task detected ({}), selecting DEEP_ANALYSIS_PATH", fileCount);
            return ToolStrategy.DEEP_ANALYSIS_PATH;
        }
        
        // Rule: Failure-prone task → SAFE_PATH
        if (successRate < 0.6) {
            log.info("Low success rate ({}), selecting SAFE_PATH strategy", successRate);
            return ToolStrategy.SAFE_PATH;
        }
        
        // Rule: High success history → FAST_PATH
        if (successRate >= 0.8) {
            log.info("High success rate ({}), selecting FAST_PATH strategy", successRate);
            return ToolStrategy.FAST_PATH;
        }
        
        // Default: SAFE_PATH
        log.info("Default strategy: SAFE_PATH");
        return ToolStrategy.SAFE_PATH;
    }
}
