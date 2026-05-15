package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionInsight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionInsightServiceTest {
    private ExecutionGraphService graphService;
    private ExecutionScoringService scoringService;
    private ExecutionInsightService insightService;

    @BeforeEach
    void setUp() {
        graphService = new ExecutionGraphService();
        scoringService = new ExecutionScoringService(graphService);
        insightService = new ExecutionInsightService(graphService, scoringService);
    }

    @Test
    void testNoExecutions() {
        List<ExecutionInsight> insights = insightService.analyze(null);
        
        assertTrue(insights.isEmpty());
    }

    @Test
    void testSingleExecution() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-001", "PATCH", "Applied cache");

        List<ExecutionInsight> insights = insightService.analyze(null);

        assertTrue(insights.isEmpty());
    }

    @Test
    void testRetryHeavyData() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "RETRY", "Retry 1");
        graphService.addNode("exec-001", "RETRY", "Retry 2");

        graphService.create("exec-002");
        graphService.addNode("exec-002", "RETRY", "Retry 1");
        graphService.addNode("exec-002", "RETRY", "Retry 2");

        List<ExecutionInsight> insights = insightService.analyze(null);

        assertTrue(insights.stream().anyMatch(i -> "RETRY_PATTERN".equals(i.getType())));
        ExecutionInsight retryInsight = insights.stream()
                .filter(i -> "RETRY_PATTERN".equals(i.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(retryInsight);
        assertEquals(2, retryInsight.getOccurrenceCount());
        assertTrue(retryInsight.getExecutionIds().contains("exec-001"));
        assertTrue(retryInsight.getExecutionIds().contains("exec-002"));
    }

    @Test
    void testFailureHeavyData() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "FAILURE", "Error 1");

        graphService.create("exec-002");
        graphService.addNode("exec-002", "FAILURE", "Error 2");

        graphService.create("exec-003");
        graphService.addNode("exec-003", "CONTEXT", "Success");

        List<ExecutionInsight> insights = insightService.analyze(null);

        assertTrue(insights.stream().anyMatch(i -> "FAILURE_CLUSTER".equals(i.getType())));
        ExecutionInsight failureInsight = insights.stream()
                .filter(i -> "FAILURE_CLUSTER".equals(i.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(failureInsight);
        assertEquals(2, failureInsight.getOccurrenceCount());
    }

    @Test
    void testLowScoreData() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "FAILURE", "Error");

        graphService.create("exec-002");
        graphService.addNode("exec-002", "RETRY", "Retry 1");
        graphService.addNode("exec-002", "RETRY", "Retry 2");
        graphService.addNode("exec-002", "FAILURE", "Error");

        List<ExecutionInsight> insights = insightService.analyze(null);

        assertTrue(insights.stream().anyMatch(i -> "LOW_QUALITY_PATTERN".equals(i.getType())));
        ExecutionInsight lowScoreInsight = insights.stream()
                .filter(i -> "LOW_QUALITY_PATTERN".equals(i.getType()))
                .findFirst()
                .orElse(null);
        assertNotNull(lowScoreInsight);
        assertTrue(lowScoreInsight.getMessage().contains("Average execution score below threshold"));
    }
}
