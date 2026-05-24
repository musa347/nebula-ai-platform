package com.aiagent.orchestrator.escalation;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiBudgetTracker {

    private final Map<String, Integer> executionBudgets = new ConcurrentHashMap<>();

    public void initializeBudget(String executionId, int maxCalls) {
        executionBudgets.put(executionId, maxCalls);
    }

    public boolean canUseAi(String executionId) {
        Integer remaining = executionBudgets.get(executionId);
        return remaining != null && remaining > 0;
    }

    public void consumeAiCall(String executionId) {
        executionBudgets.computeIfPresent(executionId, (id, remaining) -> remaining - 1);
    }

    public int getRemainingBudget(String executionId) {
        return executionBudgets.getOrDefault(executionId, 0);
    }

    public void clearBudget(String executionId) {
        executionBudgets.remove(executionId);
    }
}
