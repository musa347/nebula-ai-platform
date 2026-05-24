package com.aiagent.orchestrator.escalation;

import org.springframework.stereotype.Component;

@Component
public class AiExecutionPolicy {

    public boolean shouldUseAiPlanning(TaskComplexity complexity) {
        return complexity == TaskComplexity.COMPLEX;
    }

    public boolean shouldUseAiPatching(TaskComplexity complexity) {
        return complexity != TaskComplexity.SIMPLE;
    }

    public boolean shouldUseAiRecovery(TaskComplexity complexity) {
        return complexity == TaskComplexity.COMPLEX;
    }

    public boolean shouldUseSemanticMemory(TaskComplexity complexity) {
        return true; // Always beneficial
    }

    public int getMaxAiCalls(TaskComplexity complexity) {
        return switch (complexity) {
            case SIMPLE -> 1;
            case MODERATE -> 2;
            case COMPLEX -> 5;
        };
    }
}
