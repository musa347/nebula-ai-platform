package com.aiagent.orchestrator.adaptive;

import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.learning.LearningSignalStore;
import com.aiagent.orchestrator.memory.MemoryMatch;
import com.aiagent.orchestrator.memory.SemanticMemoryQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionRiskAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(ExecutionRiskAnalyzer.class);
    
    private final SemanticMemoryQueryService memoryQuery;
    private final LearningSignalStore learningStore;
    
    public ExecutionRiskAnalyzer(SemanticMemoryQueryService memoryQuery, LearningSignalStore learningStore) {
        this.memoryQuery = memoryQuery;
        this.learningStore = learningStore;
    }
    
    public ExecutionRisk analyzeRisk(String task, String toolName) {
        double riskScore = 0.0;
        StringBuilder reason = new StringBuilder();
        
        // Rule 1: Similar past failures
        List<MemoryMatch> similarTasks = memoryQuery.query(task, 3);
        long failureCount = similarTasks.stream()
                .filter(m -> "FAILURE".equals(m.getOutcome()))
                .count();
        
        if (failureCount > 0) {
            riskScore += 0.3;
            reason.append("Similar past failures detected. ");
        }
        
        // Rule 2: Tool failure history
        List<LearningSignal> toolHistory = learningStore.filterByTool(toolName);
        if (!toolHistory.isEmpty()) {
            long toolFailures = toolHistory.stream()
                    .filter(signal -> !signal.isSuccess())
                    .count();
            double toolFailureRate = (double) toolFailures / toolHistory.size();
            
            if (toolFailureRate > 0.3) {
                riskScore += 0.3;
                reason.append("Tool has high failure rate. ");
            }
        }
        
        // Rule 3: Low semantic match
        if (!similarTasks.isEmpty()) {
            double bestMatch = similarTasks.get(0).getScore();
            if (bestMatch < 0.5) {
                riskScore += 0.2;
                reason.append("Low semantic match with past tasks. ");
            }
        } else {
            riskScore += 0.1;
            reason.append("No similar past tasks found. ");
        }
        
        riskScore = Math.min(1.0, riskScore);
        
        if (reason.length() == 0) {
            reason.append("Low risk - known success pattern");
        }
        
        log.info("Risk analysis: task='{}', tool='{}', risk={}, reason='{}'", 
                 task, toolName, riskScore, reason.toString().trim());
        
        return new ExecutionRisk(riskScore, reason.toString().trim());
    }
}
