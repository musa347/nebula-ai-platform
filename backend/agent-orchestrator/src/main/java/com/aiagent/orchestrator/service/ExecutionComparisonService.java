package com.aiagent.orchestrator.service;

import com.aiagent.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ExecutionComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionComparisonService.class);
    private final ExecutionGraphService executionGraphService;

    public ExecutionComparisonService(ExecutionGraphService executionGraphService) {
        this.executionGraphService = executionGraphService;
    }

    public ComparisonResult compare(String executionA, String executionB) {
        ComparisonResult result = new ComparisonResult(executionA, executionB);

        ExecutionGraph graphA = executionGraphService.get(executionA);
        ExecutionGraph graphB = executionGraphService.get(executionB);

        if (graphA == null || graphB == null) {
            logger.debug("One or both executions not found: {} / {}", executionA, executionB);
            return result;
        }

        List<ExecutionNode> nodesA = graphA.getNodes().stream()
                .sorted(Comparator.comparing(ExecutionNode::getTimestamp))
                .toList();

        List<ExecutionNode> nodesB = graphB.getNodes().stream()
                .sorted(Comparator.comparing(ExecutionNode::getTimestamp))
                .toList();

        int maxSize = Math.max(nodesA.size(), nodesB.size());

        for (int i = 0; i < maxSize; i++) {
            ExecutionNode nodeA = i < nodesA.size() ? nodesA.get(i) : null;
            ExecutionNode nodeB = i < nodesB.size() ? nodesB.get(i) : null;

            if (nodeA != null && nodeB == null) {
                result.getDifferences().add(new Difference(
                        "MISSING",
                        nodeA.getType(),
                        nodeA.getMessage(),
                        null
                ));
            } else if (nodeA == null && nodeB != null) {
                result.getDifferences().add(new Difference(
                        "EXTRA",
                        nodeB.getType(),
                        null,
                        nodeB.getMessage()
                ));
            } else if (nodeA != null && nodeB != null) {
                if (!nodeA.getType().equals(nodeB.getType()) || !nodeA.getMessage().equals(nodeB.getMessage())) {
                    result.getDifferences().add(new Difference(
                            "CHANGED",
                            nodeA.getType(),
                            nodeA.getMessage(),
                            nodeB.getMessage()
                    ));
                }
            }
        }

        logger.debug("Compared {} vs {}: {} differences", executionA, executionB, result.getDifferences().size());
        return result;
    }
}
