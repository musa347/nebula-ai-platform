package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ComparisonResult;
import com.aiagent.common.model.Difference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionComparisonServiceTest {
    private ExecutionGraphService graphService;
    private ExecutionComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        graphService = new ExecutionGraphService();
        comparisonService = new ExecutionComparisonService(graphService);
    }

    @Test
    void testIdenticalExecutions() {
        graphService.create("exec-A");
        graphService.addNode("exec-A", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-A", "PATCH", "Applied cache");

        graphService.create("exec-B");
        graphService.addNode("exec-B", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-B", "PATCH", "Applied cache");

        ComparisonResult result = comparisonService.compare("exec-A", "exec-B");

        assertEquals("exec-A", result.getExecutionA());
        assertEquals("exec-B", result.getExecutionB());
        assertTrue(result.getDifferences().isEmpty());
    }

    @Test
    void testOneMissingNode() {
        graphService.create("exec-A");
        graphService.addNode("exec-A", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-A", "PATCH", "Applied cache");

        graphService.create("exec-B");
        graphService.addNode("exec-B", "CONTEXT", "UserService loaded");

        ComparisonResult result = comparisonService.compare("exec-A", "exec-B");

        assertEquals(1, result.getDifferences().size());
        Difference diff = result.getDifferences().get(0);
        assertEquals("MISSING", diff.getType());
        assertEquals("PATCH", diff.getNodeType());
        assertEquals("Applied cache", diff.getMessageA());
        assertNull(diff.getMessageB());
    }

    @Test
    void testOneExtraNode() {
        graphService.create("exec-A");
        graphService.addNode("exec-A", "CONTEXT", "UserService loaded");

        graphService.create("exec-B");
        graphService.addNode("exec-B", "CONTEXT", "UserService loaded");
        graphService.addNode("exec-B", "RETRY", "Retry attempt 1");

        ComparisonResult result = comparisonService.compare("exec-A", "exec-B");

        assertEquals(1, result.getDifferences().size());
        Difference diff = result.getDifferences().get(0);
        assertEquals("EXTRA", diff.getType());
        assertEquals("RETRY", diff.getNodeType());
        assertNull(diff.getMessageA());
        assertEquals("Retry attempt 1", diff.getMessageB());
    }

    @Test
    void testDifferentMessages() {
        graphService.create("exec-A");
        graphService.addNode("exec-A", "CONTEXT", "UserService");

        graphService.create("exec-B");
        graphService.addNode("exec-B", "CONTEXT", "UserService + CacheService");

        ComparisonResult result = comparisonService.compare("exec-A", "exec-B");

        assertEquals(1, result.getDifferences().size());
        Difference diff = result.getDifferences().get(0);
        assertEquals("CHANGED", diff.getType());
        assertEquals("CONTEXT", diff.getNodeType());
        assertEquals("UserService", diff.getMessageA());
        assertEquals("UserService + CacheService", diff.getMessageB());
    }

    @Test
    void testEmptyExecution() {
        graphService.create("exec-A");
        graphService.create("exec-B");

        ComparisonResult result = comparisonService.compare("exec-A", "exec-B");

        assertEquals("exec-A", result.getExecutionA());
        assertEquals("exec-B", result.getExecutionB());
        assertTrue(result.getDifferences().isEmpty());
    }

    @Test
    void testMissingExecutionId() {
        graphService.create("exec-A");
        graphService.addNode("exec-A", "CONTEXT", "Test");

        ComparisonResult result = comparisonService.compare("exec-A", "non-existent");

        assertEquals("exec-A", result.getExecutionA());
        assertEquals("non-existent", result.getExecutionB());
        assertTrue(result.getDifferences().isEmpty());
    }
}
