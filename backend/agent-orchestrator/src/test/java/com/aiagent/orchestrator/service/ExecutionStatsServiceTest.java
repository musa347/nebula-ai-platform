package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolExecutionStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStatsServiceTest {

    private ExecutionStatsService executionStatsService;

    @BeforeEach
    void setUp() {
        executionStatsService = new ExecutionStatsService();
    }

    @Test
    void testSuccessTracking() {
        // Record multiple successes
        executionStatsService.recordSuccess(ToolType.FILE_READ, 100);
        executionStatsService.recordSuccess(ToolType.FILE_READ, 200);
        
        ToolExecutionStats stats = executionStatsService.getStats(ToolType.FILE_READ);
        
        assertEquals(2, stats.getSuccessCount());
        assertEquals(0, stats.getFailureCount());
        assertEquals(1.0, stats.getSuccessRate(), 0.01);
        assertEquals(150, stats.getAvgExecutionTimeMs()); // (100 + 200) / 2
    }

    @Test
    void testFailureTracking() {
        // Record multiple failures
        executionStatsService.recordFailure(ToolType.PATCH_APPLY, 300);
        executionStatsService.recordFailure(ToolType.PATCH_APPLY, 400);
        
        ToolExecutionStats stats = executionStatsService.getStats(ToolType.PATCH_APPLY);
        
        assertEquals(0, stats.getSuccessCount());
        assertEquals(2, stats.getFailureCount());
        assertEquals(0.0, stats.getSuccessRate(), 0.01);
        assertEquals(1.0, stats.getFailureRate(), 0.01);
        assertEquals(350, stats.getAvgExecutionTimeMs()); // (300 + 400) / 2
    }

    @Test
    void testMixedSuccessFailure() {
        // Record mixed results
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 100);
        executionStatsService.recordFailure(ToolType.SYMBOL_SEARCH, 200);
        executionStatsService.recordSuccess(ToolType.SYMBOL_SEARCH, 300);
        
        ToolExecutionStats stats = executionStatsService.getStats(ToolType.SYMBOL_SEARCH);
        
        assertEquals(2, stats.getSuccessCount());
        assertEquals(1, stats.getFailureCount());
        assertEquals(0.67, stats.getSuccessRate(), 0.01);
        assertEquals(0.33, stats.getFailureRate(), 0.01);
        assertEquals(200, stats.getAvgExecutionTimeMs()); // (100 + 200 + 300) / 3
    }

    @Test
    void testEmptyStatsDefault() {
        // Get stats for tool with no recorded executions
        ToolExecutionStats stats = executionStatsService.getStats(ToolType.REPO_SEARCH);
        
        assertEquals(0, stats.getSuccessCount());
        assertEquals(0, stats.getFailureCount());
        assertEquals(0.0, stats.getSuccessRate());
        assertEquals(0.0, stats.getFailureRate());
        assertEquals(0, stats.getAvgExecutionTimeMs());
    }

    @Test
    void testConcurrentAccess() {
        // Test thread safety with concurrent updates
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                executionStatsService.recordSuccess(ToolType.FILE_READ, 100);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                executionStatsService.recordFailure(ToolType.FILE_READ, 200);
            }
        });
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
        
        ToolExecutionStats stats = executionStatsService.getStats(ToolType.FILE_READ);
        assertEquals(15, stats.getSuccessCount() + stats.getFailureCount());
        assertEquals(10, stats.getSuccessCount());
        assertEquals(5, stats.getFailureCount());
    }
}