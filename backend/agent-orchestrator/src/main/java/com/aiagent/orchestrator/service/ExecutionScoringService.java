package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionGraph;
import com.aiagent.common.model.ExecutionNode;
import com.aiagent.common.model.ExecutionScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionScoringService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionScoringService.class);
    private final ExecutionGraphService executionGraphService;

    private static final double BASE_SCORE = 0.5;
    private static final double RETRY_PENALTY = 0.15;
    private static final double FAILURE_PENALTY = 0.25;
    private static final double PATCH_BONUS = 0.3;
    private static final double CONTEXT_EFFICIENCY_BONUS = 0.1;
    private static final double CONTEXT_INEFFICIENCY_PENALTY = 0.1;
    private static final double STABILITY_BONUS = 0.2;

    public ExecutionScoringService(ExecutionGraphService executionGraphService) {
        this.executionGraphService = executionGraphService;
    }

    public ExecutionScore score(String executionId) {
        ExecutionScore result = new ExecutionScore(executionId, BASE_SCORE);
        double score = BASE_SCORE;

        ExecutionGraph graph = executionGraphService.get(executionId);
        if (graph == null) {
            logger.debug("No execution graph found for: {}", executionId);
            result.setScore(score);
            return result;
        }

        List<ExecutionNode> nodes = graph.getNodes();

        // Rule 1: Retry Penalty
        long retryCount = nodes.stream().filter(n -> "RETRY".equals(n.getType())).count();
        if (retryCount > 0) {
            double penalty = retryCount * RETRY_PENALTY;
            score -= penalty;
            result.getReasons().add(retryCount + " retry detected (-" + penalty + ")");
        }

        // Rule 2: Failure Penalty
        boolean hasFailure = nodes.stream().anyMatch(n -> "FAILURE".equals(n.getType()));
        if (hasFailure) {
            score -= FAILURE_PENALTY;
            result.getReasons().add("Failure detected (-" + FAILURE_PENALTY + ")");
        }

        // Rule 3: Successful Patch Bonus
        boolean hasPatch = nodes.stream().anyMatch(n -> "PATCH".equals(n.getType()));
        if (hasPatch) {
            score += PATCH_BONUS;
            result.getReasons().add("Patch applied successfully (+" + PATCH_BONUS + ")");
        }

        // Rule 4: Context Efficiency
        long contextCount = nodes.stream().filter(n -> "CONTEXT".equals(n.getType())).count();
        if (contextCount <= 3 && contextCount > 0) {
            score += CONTEXT_EFFICIENCY_BONUS;
            result.getReasons().add("Low context usage (+" + CONTEXT_EFFICIENCY_BONUS + ")");
        } else if (contextCount > 8) {
            score -= CONTEXT_INEFFICIENCY_PENALTY;
            result.getReasons().add("High context usage (-" + CONTEXT_INEFFICIENCY_PENALTY + ")");
        }

        // Rule 5: Execution Stability Bonus
        if (retryCount == 0 && !hasFailure) {
            score += STABILITY_BONUS;
            result.getReasons().add("Stable execution (+" + STABILITY_BONUS + ")");
        }

        // Clamp score between 0.0 and 1.0
        score = Math.max(0.0, Math.min(1.0, score));
        result.setScore(score);

        logger.debug("Scored execution {}: {}", executionId, score);
        return result;
    }
}
