package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionGraph;
import com.aiagent.common.model.ExecutionNode;
import com.aiagent.common.model.ReplayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ExecutionReplayService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionReplayService.class);
    private final ExecutionGraphService executionGraphService;

    public ExecutionReplayService(ExecutionGraphService executionGraphService) {
        this.executionGraphService = executionGraphService;
    }

    public List<ReplayEvent> replay(String executionId) {
        ExecutionGraph graph = executionGraphService.get(executionId);

        if (graph == null) {
            logger.debug("No execution graph found for: {}", executionId);
            return List.of();
        }

        return graph.getNodes().stream()
                .sorted(Comparator.comparing(ExecutionNode::getTimestamp))
                .map(node -> new ReplayEvent(
                        executionId,
                        node.getType(),
                        node.getMessage(),
                        node.getTimestamp()
                ))
                .toList();
    }
}
