package com.aiagent.orchestrator.escalation;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.orchestrator.autonomous.AutonomousOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ORCH-018 Integration Test
 * Demonstrates deterministic-first AI escalation performance improvements
 */
@SpringBootTest
class EscalationIntegrationTest {

    @Autowired
    private AutonomousOrchestrator orchestrator;

    @Autowired
    private TaskComplexityAnalyzer complexityAnalyzer;

    @Autowired
    private AiBudgetTracker budgetTracker;

    @Test
    void testSimpleTask_SkipsAiPlanning() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("add logging to UserService");

        TaskComplexity complexity = complexityAnalyzer.analyze(request.getTask());
        assertEquals(TaskComplexity.SIMPLE, complexity);

        long startTime = System.currentTimeMillis();
        OrchestratorTaskResponse response = orchestrator.execute(request);
        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(response);
        assertNotNull(response.getExecutionId());

        System.out.println("SIMPLE task duration: " + duration + "ms");
        assertTrue(duration < 5000, "SIMPLE task should complete quickly");
    }

    @Test
    void testModerateTask_UsesSelectiveAi() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("refactor payment retry flow");

        TaskComplexity complexity = complexityAnalyzer.analyze(request.getTask());
        assertEquals(TaskComplexity.MODERATE, complexity);

        OrchestratorTaskResponse response = orchestrator.execute(request);

        assertNotNull(response);
        System.out.println("MODERATE task completed with execution ID: " + response.getExecutionId());
    }

    @Test
    void testComplexTask_UsesFullAi() {
        OrchestratorTaskRequest request = new OrchestratorTaskRequest("redesign authentication architecture");

        TaskComplexity complexity = complexityAnalyzer.analyze(request.getTask());
        assertEquals(TaskComplexity.COMPLEX, complexity);

        OrchestratorTaskResponse response = orchestrator.execute(request);

        assertNotNull(response);
        System.out.println("COMPLEX task completed with execution ID: " + response.getExecutionId());
    }

    @Test
    void testAiBudgetEnforcement() {
        String executionId = "test-budget-exec";
        budgetTracker.initializeBudget(executionId, 3);

        assertEquals(3, budgetTracker.getRemainingBudget(executionId));

        budgetTracker.consumeAiCall(executionId);
        budgetTracker.consumeAiCall(executionId);
        budgetTracker.consumeAiCall(executionId);

        assertEquals(0, budgetTracker.getRemainingBudget(executionId));
        assertFalse(budgetTracker.canUseAi(executionId));

        budgetTracker.clearBudget(executionId);
    }

    @Test
    void testPerformanceComparison() {
        OrchestratorTaskRequest simpleRequest = new OrchestratorTaskRequest("add cache");
        long simpleStart = System.currentTimeMillis();
        OrchestratorTaskResponse simpleResponse = orchestrator.execute(simpleRequest);
        long simpleDuration = System.currentTimeMillis() - simpleStart;


        OrchestratorTaskRequest complexRequest = new OrchestratorTaskRequest("implement event sourcing");
        long complexStart = System.currentTimeMillis();
        OrchestratorTaskResponse complexResponse = orchestrator.execute(complexRequest);
        long complexDuration = System.currentTimeMillis() - complexStart;

        System.out.println("\n=== ORCH-018 Performance Comparison ===");
        System.out.println("SIMPLE task: " + simpleDuration + "ms");
        System.out.println("COMPLEX task: " + complexDuration + "ms");
        System.out.println("Speedup ratio: " + (complexDuration / (double) simpleDuration) + "x");
        System.out.println("========================================\n");

        assertNotNull(simpleResponse);
        assertNotNull(complexResponse);

        assertTrue(simpleDuration < complexDuration,
                "SIMPLE task should be faster than COMPLEX task");
    }
}
