package com.aiagent.orchestrator.service;

import com.aiagent.common.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExecutionInsightService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionInsightService.class);
    private final ExecutionGraphService executionGraphService;
    private final ExecutionScoringService executionScoringService;

    public ExecutionInsightService(ExecutionGraphService executionGraphService,
                                   ExecutionScoringService executionScoringService) {
        this.executionGraphService = executionGraphService;
        this.executionScoringService = executionScoringService;
    }

    public List<ExecutionInsight> analyze(Integer limit) {
        List<ExecutionInsight> insights = new ArrayList<>();

        Map<String, ExecutionGraph> allGraphs = executionGraphService.getAll();
        if (allGraphs.isEmpty()) {
            return insights;
        }

        List<String> executionIds = new ArrayList<>(allGraphs.keySet());
        if (limit != null && limit > 0 && limit < executionIds.size()) {
            executionIds = executionIds.subList(0, limit);
        }

        // Rule 1: Retry Pattern Detection
        List<String> retryExecutions = new ArrayList<>();
        for (String execId : executionIds) {
            ExecutionGraph graph = allGraphs.get(execId);
            long retryCount = graph.getNodes().stream()
                    .filter(n -> "RETRY".equals(n.getType()))
                    .count();
            if (retryCount >= 2) {
                retryExecutions.add(execId);
            }
        }

        if (!retryExecutions.isEmpty()) {
            ExecutionInsight insight = new ExecutionInsight(
                    "RETRY_PATTERN",
                    "Frequent retries detected in executions",
                    retryExecutions.size()
            );
            insight.setExecutionIds(retryExecutions);
            insights.add(insight);
        }

        // Rule 2: Failure Concentration
        List<String> failureExecutions = new ArrayList<>();
        for (String execId : executionIds) {
            ExecutionGraph graph = allGraphs.get(execId);
            boolean hasFailure = graph.getNodes().stream()
                    .anyMatch(n -> "FAILURE".equals(n.getType()));
            if (hasFailure) {
                failureExecutions.add(execId);
            }
        }

        if (failureExecutions.size() > executionIds.size() * 0.5) {
            ExecutionInsight insight = new ExecutionInsight(
                    "FAILURE_CLUSTER",
                    "High failure rate detected (>50% of executions)",
                    failureExecutions.size()
            );
            insight.setExecutionIds(failureExecutions);
            insights.add(insight);
        }

        // Rule 3: Low Score Correlation
        List<String> lowScoreExecutions = new ArrayList<>();
        double totalScore = 0.0;
        for (String execId : executionIds) {
            ExecutionScore score = executionScoringService.score(execId);
            totalScore += score.getScore();
            if (score.getScore() < 0.5) {
                lowScoreExecutions.add(execId);
            }
        }

        double avgScore = totalScore / executionIds.size();
        if (avgScore < 0.5) {
            ExecutionInsight insight = new ExecutionInsight(
                    "LOW_QUALITY_PATTERN",
                    "Average execution score below threshold (avg: " + String.format("%.2f", avgScore) + ")",
                    lowScoreExecutions.size()
            );
            insight.setExecutionIds(lowScoreExecutions);
            insights.add(insight);
        }

        logger.debug("Generated {} insights from {} executions", insights.size(), executionIds.size());
        return insights;
    }
}
