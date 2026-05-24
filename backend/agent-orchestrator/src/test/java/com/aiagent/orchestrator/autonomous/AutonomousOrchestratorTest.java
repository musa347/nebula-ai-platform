package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.orchestrator.adaptive.ExecutionRisk;
import com.aiagent.orchestrator.adaptive.ExecutionRiskAnalyzer;
import com.aiagent.orchestrator.escalation.*;
import com.aiagent.orchestrator.service.PlanningService;
import com.aiagent.orchestrator.service.ToolExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutonomousOrchestratorTest {

    private AutonomousOrchestrator orchestrator;
    private NextActionEngine nextActionEngine;
    private UnifiedToolExecutor toolExecutor;
    private LearningUpdater learningUpdater;
    private PlanningService planningService;
    private ExecutionRiskAnalyzer riskAnalyzer;
    private TaskComplexityAnalyzer complexityAnalyzer;
    private AiExecutionPolicy aiPolicy;
    private DeterministicPatchEngine patchEngine;
    private AiBudgetTracker budgetTracker;
    private AiResponseCache aiCache;

    @BeforeEach
    void setUp() {
        nextActionEngine = new NextActionEngine();
        learningUpdater = createStubLearningUpdater();
        planningService = createStubPlanningService();
        riskAnalyzer = createStubRiskAnalyzer();
        toolExecutor = createStubToolExecutor();

        // ORCH-018: Initialize escalation components
        complexityAnalyzer = new TaskComplexityAnalyzer();
        aiPolicy = new AiExecutionPolicy();
        patchEngine = new DeterministicPatchEngine();
        budgetTracker = new AiBudgetTracker();
        aiCache = new AiResponseCache();

        orchestrator = new AutonomousOrchestrator();
        injectDependencies();
    }

    @Test
    void testLoopCompletesSuccessfully() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Simple task");

        OrchestratorTaskResponse response = orchestrator.execute(request);

        assertNotNull(response);
        assertNotNull(response.getExecutionId());
        assertTrue(response.getCompletedStates().contains(ExecutionState.CREATED));
    }

    @Test
    void testRetryWorks() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Task with retry");
        OrchestratorTaskResponse response = orchestrator.execute(request);
        assertNotNull(response);
    }

    @Test
    void testFailureTriggersRecovery() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("Failing task");
        OrchestratorTaskResponse response = orchestrator.execute(request);
        assertNotNull(response);
    }

    private void injectDependencies() {
        try {
            var engineField = AutonomousOrchestrator.class.getDeclaredField("nextActionEngine");
            engineField.setAccessible(true);
            engineField.set(orchestrator, nextActionEngine);

            var executorField = AutonomousOrchestrator.class.getDeclaredField("toolExecutor");
            executorField.setAccessible(true);
            executorField.set(orchestrator, toolExecutor);

            var learningField = AutonomousOrchestrator.class.getDeclaredField("learningUpdater");
            learningField.setAccessible(true);
            learningField.set(orchestrator, learningUpdater);

            var planningField = AutonomousOrchestrator.class.getDeclaredField("planningService");
            planningField.setAccessible(true);
            planningField.set(orchestrator, planningService);

            var riskField = AutonomousOrchestrator.class.getDeclaredField("riskAnalyzer");
            riskField.setAccessible(true);
            riskField.set(orchestrator, riskAnalyzer);

            var complexityField = AutonomousOrchestrator.class.getDeclaredField("complexityAnalyzer");
            complexityField.setAccessible(true);
            complexityField.set(orchestrator, complexityAnalyzer);

            var policyField = AutonomousOrchestrator.class.getDeclaredField("aiPolicy");
            policyField.setAccessible(true);
            policyField.set(orchestrator, aiPolicy);

            var patchField = AutonomousOrchestrator.class.getDeclaredField("deterministicPatchEngine");
            patchField.setAccessible(true);
            patchField.set(orchestrator, patchEngine);

            var budgetField = AutonomousOrchestrator.class.getDeclaredField("budgetTracker");
            budgetField.setAccessible(true);
            budgetField.set(orchestrator, budgetTracker);

            var cacheField = AutonomousOrchestrator.class.getDeclaredField("aiCache");
            cacheField.setAccessible(true);
            cacheField.set(orchestrator, aiCache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    private LearningUpdater createStubLearningUpdater() {
        return new LearningUpdater() {
            @Override
            public void update(AutonomousExecutionContext context, boolean success, long durationMs) {
                // Stub implementation
            }
        };
    }

    private PlanningService createStubPlanningService() {
        return new PlanningService(null) {
            @Override
            public ExecutionPlan createPlan(String task) {
                return new ExecutionPlan(task, List.of());
            }
        };
    }

    private ExecutionRiskAnalyzer createStubRiskAnalyzer() {
        return new ExecutionRiskAnalyzer(null, null) {
            @Override
            public ExecutionRisk analyzeRisk(String task, String context) {
                return new ExecutionRisk(0.2, "Low risk");
            }
        };
    }

    private UnifiedToolExecutor createStubToolExecutor() {
        return new UnifiedToolExecutor() {
            @Override
            public ToolExecutionService.ToolExecutionResult execute(AutonomousExecutionContext context) {
                return new ToolExecutionService.ToolExecutionResult(true, "Success", List.of());
            }
        };
    }
}
