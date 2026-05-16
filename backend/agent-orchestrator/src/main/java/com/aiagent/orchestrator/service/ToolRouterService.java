package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import com.aiagent.common.model.ToolExecutionStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ToolRouterService {

    private static final Logger log = LoggerFactory.getLogger(ToolRouterService.class);
    
    @Autowired
    private ExecutionStatsService executionStatsService;
    
    // State to candidate tools mapping
    private static final Map<ExecutionState, List<ToolType>> STATE_TOOL_CANDIDATES = Map.of(
        ExecutionState.PLANNING, List.of(ToolType.REPO_SEARCH),
        ExecutionState.CONTEXT_LOADING, List.of(ToolType.SYMBOL_SEARCH, ToolType.DEPENDENCY_ANALYSIS),
        ExecutionState.EXECUTING, List.of(ToolType.FILE_READ),
        ExecutionState.VERIFYING, List.of(ToolType.PATCH_GENERATE),
        ExecutionState.PATCH_APPLYING, List.of(ToolType.PATCH_APPLY)
    );

    public ToolDecision decide(ExecutionState currentState, String task, String availableContext) {
        if (currentState == null) {
            return new ToolDecision(ToolType.NONE, "No state provided");
        }

        ToolDecision decision = routeByStateWithStats(currentState, task, availableContext);
        log.debug("Adaptive tool routing for state {}: {} - {}", currentState, decision.getToolType(), decision.getReason());
        return decision;
    }

    private ToolDecision routeByStateWithStats(ExecutionState state, String task, String availableContext) {
        List<ToolType> candidates = STATE_TOOL_CANDIDATES.get(state);
        
        if (candidates == null || candidates.isEmpty()) {
            return new ToolDecision(ToolType.NONE, "No tools available for state: " + state);
        }
        
        if (candidates.size() == 1) {
            // Single candidate - no need for scoring
            return new ToolDecision(candidates.get(0), "Only tool available for state: " + state);
        }
        
        // Multiple candidates - use adaptive scoring
        ToolType bestTool = selectBestTool(candidates, task);
        return new ToolDecision(bestTool, "Best performing tool for state: " + state);
    }
    
    private ToolType selectBestTool(List<ToolType> candidates, String task) {
        ToolType bestTool = candidates.get(0);
        double bestScore = calculateToolScore(bestTool, task);
        
        for (int i = 1; i < candidates.size(); i++) {
            ToolType candidate = candidates.get(i);
            double score = calculateToolScore(candidate, task);
            
            if (score > bestScore) {
                bestScore = score;
                bestTool = candidate;
            }
        }
        
        return bestTool;
    }
    
    private double calculateToolScore(ToolType tool, String task) {
        ToolExecutionStats stats = executionStatsService.getStats(tool);
        
        // Base score from success rate
        double successRate = stats.getSuccessRate();
        double failureRate = stats.getFailureRate();
        
        // Latency penalty (normalize to 0-1 scale, penalize > 1000ms)
        double latencyPenalty = Math.min(stats.getAvgExecutionTimeMs() / 1000.0, 1.0) * 0.2;
        
        // Task-specific bonus (simple keyword matching)
        double taskBonus = getTaskBonus(tool, task);
        
        // Final score: successRate - (failureRate * 0.5) - latencyPenalty + taskBonus
        return successRate - (failureRate * 0.5) - latencyPenalty + taskBonus;
    }
    
    private double getTaskBonus(ToolType tool, String task) {
        if (task == null) return 0.0;
        
        String lowerTask = task.toLowerCase();
        
        switch (tool) {
            case DEPENDENCY_ANALYSIS:
                return (lowerTask.contains("dependency") || lowerTask.contains("import")) ? 0.1 : 0.0;
            case SYMBOL_SEARCH:
                return (lowerTask.contains("symbol") || lowerTask.contains("method")) ? 0.1 : 0.0;
            default:
                return 0.0;
        }
    }


}