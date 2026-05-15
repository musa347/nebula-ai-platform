package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionGraph;
import com.aiagent.common.model.ExecutionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutionGraphService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionGraphService.class);
    private final Map<String, ExecutionGraph> store = new ConcurrentHashMap<>();

    public void create(String executionId) {
        ExecutionGraph graph = new ExecutionGraph(executionId);
        store.put(executionId, graph);
        logger.debug("Created execution graph: {}", executionId);
    }

    public void addNode(String executionId, String type, String message) {
        ExecutionGraph graph = store.get(executionId);
        if (graph == null) {
            logger.warn("Execution graph not found: {}", executionId);
            return;
        }
        ExecutionNode node = new ExecutionNode(type, message, System.currentTimeMillis());
        graph.getNodes().add(node);
        logger.debug("Added node to {}: {} - {}", executionId, type, message);
    }

    public ExecutionGraph get(String executionId) {
        return store.get(executionId);
    }

    public Map<String, ExecutionGraph> getAll() {
        return store;
    }
}
