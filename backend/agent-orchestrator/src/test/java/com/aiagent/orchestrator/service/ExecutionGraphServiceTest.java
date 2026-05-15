package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionGraph;
import com.aiagent.common.model.ExecutionNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionGraphServiceTest {
    private ExecutionGraphService service;

    @BeforeEach
    void setUp() {
        service = new ExecutionGraphService();
    }

    @Test
    void testGraphCreated() {
        service.create("exec-001");
        ExecutionGraph graph = service.get("exec-001");
        
        assertNotNull(graph);
        assertEquals("exec-001", graph.getExecutionId());
        assertTrue(graph.getNodes().isEmpty());
    }

    @Test
    void testNodeAdded() {
        service.create("exec-002");
        service.addNode("exec-002", "CONTEXT", "UserService loaded");
        
        ExecutionGraph graph = service.get("exec-002");
        assertEquals(1, graph.getNodes().size());
        
        ExecutionNode node = graph.getNodes().get(0);
        assertEquals("CONTEXT", node.getType());
        assertEquals("UserService loaded", node.getMessage());
        assertTrue(node.getTimestamp() > 0);
    }

    @Test
    void testMultipleNodesOrderedCorrectly() {
        service.create("exec-003");
        service.addNode("exec-003", "CONTEXT", "First");
        service.addNode("exec-003", "PATCH", "Second");
        service.addNode("exec-003", "RETRY", "Third");
        
        ExecutionGraph graph = service.get("exec-003");
        assertEquals(3, graph.getNodes().size());
        assertEquals("CONTEXT", graph.getNodes().get(0).getType());
        assertEquals("PATCH", graph.getNodes().get(1).getType());
        assertEquals("RETRY", graph.getNodes().get(2).getType());
    }

    @Test
    void testExecutionIdIsolationWorks() {
        service.create("exec-004");
        service.create("exec-005");
        service.addNode("exec-004", "CONTEXT", "Graph A");
        service.addNode("exec-005", "PATCH", "Graph B");
        
        ExecutionGraph graphA = service.get("exec-004");
        ExecutionGraph graphB = service.get("exec-005");
        
        assertEquals(1, graphA.getNodes().size());
        assertEquals(1, graphB.getNodes().size());
        assertEquals("CONTEXT", graphA.getNodes().get(0).getType());
        assertEquals("PATCH", graphB.getNodes().get(0).getType());
    }

    @Test
    void testRetrievalWorks() {
        service.create("exec-006");
        service.addNode("exec-006", "TOOL_CALL", "filesystem.read");
        service.addNode("exec-006", "FAILURE", "File not found");
        
        ExecutionGraph graph = service.get("exec-006");
        assertNotNull(graph);
        assertEquals("exec-006", graph.getExecutionId());
        assertEquals(2, graph.getNodes().size());
        
        ExecutionGraph nonExistent = service.get("exec-999");
        assertNull(nonExistent);
    }
}
