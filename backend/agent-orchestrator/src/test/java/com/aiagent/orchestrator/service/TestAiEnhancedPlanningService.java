package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;

import java.util.List;

public class TestAiEnhancedPlanningService extends AiEnhancedPlanningService {
    private boolean shouldReturnEnhanced = false;
    private boolean shouldThrowException = false;
    private boolean enhanceCalled = false;

    public TestAiEnhancedPlanningService() {
        super(null, null, null);
    }

    @Override
    public ExecutionPlan enhancePlan(String task, ExecutionPlan deterministicPlan) {
        enhanceCalled = true;
        
        if (shouldThrowException) {
            throw new RuntimeException("AI service error");
        }
        
        if (shouldReturnEnhanced) {
            ExecutionPlan enhanced = new ExecutionPlan();
            enhanced.setExecutionId("enhanced-plan");
            enhanced.setSteps(List.of(
                new PlanStep(1, "Enhanced step", ToolType.REPO_SEARCH, "enhanced")
            ));
            return enhanced;
        }
        
        return null; // Fallback to deterministic
    }

    public void setShouldReturnEnhanced(boolean shouldReturnEnhanced) {
        this.shouldReturnEnhanced = shouldReturnEnhanced;
    }

    public void setShouldThrowException(boolean shouldThrowException) {
        this.shouldThrowException = shouldThrowException;
    }

    public boolean wasEnhanceCalled() {
        return enhanceCalled;
    }
}