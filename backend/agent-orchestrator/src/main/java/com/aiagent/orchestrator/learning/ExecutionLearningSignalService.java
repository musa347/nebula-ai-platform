package com.aiagent.orchestrator.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExecutionLearningSignalService {
    private static final Logger log = LoggerFactory.getLogger(ExecutionLearningSignalService.class);
    
    public LearningSignal generateSignal(String executionId, boolean success, String toolUsed, 
                                         String state, double score, boolean hasRetries) {
        String outcome = determineOutcome(success, score, hasRetries);
        
        LearningSignal signal = new LearningSignal(executionId, success, toolUsed, state, score, outcome);
        log.debug("Generated learning signal: executionId={}, outcome={}, score={}", 
                  executionId, outcome, score);
        
        return signal;
    }
    
    private String determineOutcome(boolean success, double score, boolean hasRetries) {
        if (!success) {
            return "FAILED";
        }
        
        if (hasRetries) {
            return "DEGRADED";
        }
        
        if (score >= 0.7) {
            return "IMPROVED";
        }
        
        return "DEGRADED";
    }
}
