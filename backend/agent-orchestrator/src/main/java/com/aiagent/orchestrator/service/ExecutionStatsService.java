package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolExecutionStats;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutionStatsService {
    
    private final ConcurrentHashMap<ToolType, ToolExecutionStats> stats = new ConcurrentHashMap<>();

    public void recordSuccess(ToolType tool, long durationMs) {
        ToolExecutionStats toolStats = stats.computeIfAbsent(tool, ToolExecutionStats::new);
        
        synchronized (toolStats) {
            int newSuccessCount = toolStats.getSuccessCount() + 1;
            toolStats.setSuccessCount(newSuccessCount);
            
            // Update average execution time
            long currentAvg = toolStats.getAvgExecutionTimeMs();
            int totalExecutions = toolStats.getSuccessCount() + toolStats.getFailureCount();
            long newAvg = ((currentAvg * (totalExecutions - 1)) + durationMs) / totalExecutions;
            toolStats.setAvgExecutionTimeMs(newAvg);
        }
    }

    public void recordFailure(ToolType tool, long durationMs) {
        ToolExecutionStats toolStats = stats.computeIfAbsent(tool, ToolExecutionStats::new);
        
        synchronized (toolStats) {
            int newFailureCount = toolStats.getFailureCount() + 1;
            toolStats.setFailureCount(newFailureCount);
            
            // Update average execution time
            long currentAvg = toolStats.getAvgExecutionTimeMs();
            int totalExecutions = toolStats.getSuccessCount() + toolStats.getFailureCount();
            long newAvg = ((currentAvg * (totalExecutions - 1)) + durationMs) / totalExecutions;
            toolStats.setAvgExecutionTimeMs(newAvg);
        }
    }

    public ToolExecutionStats getStats(ToolType tool) {
        return stats.getOrDefault(tool, new ToolExecutionStats(tool));
    }
}