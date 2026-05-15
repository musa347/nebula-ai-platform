package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionScoringServiceTest {
    private ExecutionGraphService graphService;
    private ExecutionScoringService scoringService;

    @BeforeEach
    void setUp() {
        graphService = new ExecutionGraphService();
        scoringService = new ExecutionScoringService(graphService);
    }

    @Test
    void testNoFailureExecution() {
        graphService.create("exec-001");
        graphService.addNode("exec-001", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-001", "PATCH", "Applied cache");

        ExecutionScore score = scoringService.score("exec-001");

        assertTrue(score.getScore() > 0.7);
        assertTrue(score.getReasons().contains("Patch applied successfully (+0.3)"));
        assertTrue(score.getReasons().contains("Low context usage (+0.1)"));
        assertTrue(score.getReasons().contains("Stable execution (+0.2)"));
    }

    @Test
    void testWithRetry() {
        graphService.create("exec-002");
        graphService.addNode("exec-002", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-002", "RETRY", "Retry attempt 1");

        ExecutionScore score = scoringService.score("exec-002");

        assertTrue(score.getScore() < 0.5);
        assertTrue(score.getReasons().stream().anyMatch(r -> r.contains("retry detected")));
    }

    @Test
    void testWithFailureNode() {
        graphService.create("exec-003");
        graphService.addNode("exec-003", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-003", "FAILURE", "Syntax error");

        ExecutionScore score = scoringService.score("exec-003");

        assertTrue(score.getScore() < 0.5);
        assertTrue(score.getReasons().contains("Failure detected (-0.25)"));
    }

    @Test
    void testContextHeavyExecution() {
        graphService.create("exec-004");
        for (int i = 0; i < 10; i++) {
            graphService.addNode("exec-004", "CONTEXT", "File " + i);
        }

        ExecutionScore score = scoringService.score("exec-004");

        assertTrue(score.getReasons().contains("High context usage (-0.1)"));
    }

    @Test
    void testPatchSuccess() {
        graphService.create("exec-005");
        graphService.addNode("exec-005", "PATCH", "Applied Redis cache");

        ExecutionScore score = scoringService.score("exec-005");

        assertTrue(score.getScore() > 0.5);
        assertTrue(score.getReasons().contains("Patch applied successfully (+0.3)"));
    }

    @Test
    void testEmptyExecution() {
        graphService.create("exec-006");

        ExecutionScore score = scoringService.score("exec-006");

        assertEquals(0.7, score.getScore(), 0.01);
        assertTrue(score.getReasons().contains("Stable execution (+0.2)"));
    }
}
