package com.aiagent.orchestrator.escalation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EscalationLayerTest {

    private TaskComplexityAnalyzer complexityAnalyzer;
    private AiExecutionPolicy aiPolicy;
    private DeterministicPatchEngine patchEngine;
    private AiBudgetTracker budgetTracker;
    private AiResponseCache aiCache;

    @BeforeEach
    void setUp() {
        complexityAnalyzer = new TaskComplexityAnalyzer();
        aiPolicy = new AiExecutionPolicy();
        patchEngine = new DeterministicPatchEngine();
        budgetTracker = new AiBudgetTracker();
        aiCache = new AiResponseCache();
    }

    @Test
    void testComplexityAnalyzer_SimpleTask() {
        assertEquals(TaskComplexity.SIMPLE, complexityAnalyzer.analyze("add logging to UserService"));
        assertEquals(TaskComplexity.SIMPLE, complexityAnalyzer.analyze("add cache"));
        assertEquals(TaskComplexity.SIMPLE, complexityAnalyzer.analyze("add validation"));
        assertEquals(TaskComplexity.SIMPLE, complexityAnalyzer.analyze("rename method in PaymentService.java"));
    }

    @Test
    void testComplexityAnalyzer_ModerateTask() {
        assertEquals(TaskComplexity.MODERATE, complexityAnalyzer.analyze("refactor payment retry flow"));
        assertEquals(TaskComplexity.MODERATE, complexityAnalyzer.analyze("optimize repository queries"));
    }

    @Test
    void testComplexityAnalyzer_ComplexTask() {
        assertEquals(TaskComplexity.COMPLEX, complexityAnalyzer.analyze("redesign authentication architecture"));
        assertEquals(TaskComplexity.COMPLEX, complexityAnalyzer.analyze("migrate monolith to microservices"));
        assertEquals(TaskComplexity.COMPLEX, complexityAnalyzer.analyze("implement event sourcing pattern"));
    }

    @Test
    void testAiExecutionPolicy_SimpleTask() {
        TaskComplexity simple = TaskComplexity.SIMPLE;

        assertFalse(aiPolicy.shouldUseAiPlanning(simple));
        assertFalse(aiPolicy.shouldUseAiPatching(simple));
        assertFalse(aiPolicy.shouldUseAiRecovery(simple));
        assertTrue(aiPolicy.shouldUseSemanticMemory(simple));
        assertEquals(1, aiPolicy.getMaxAiCalls(simple));
    }

    @Test
    void testAiExecutionPolicy_ComplexTask() {
        TaskComplexity complex = TaskComplexity.COMPLEX;

        assertTrue(aiPolicy.shouldUseAiPlanning(complex));
        assertTrue(aiPolicy.shouldUseAiPatching(complex));
        assertTrue(aiPolicy.shouldUseAiRecovery(complex));
        assertTrue(aiPolicy.shouldUseSemanticMemory(complex));
        assertEquals(5, aiPolicy.getMaxAiCalls(complex));
    }

    @Test
    void testDeterministicPatchEngine_Confidence() {
        assertTrue(patchEngine.getConfidence("add logging") > 0.7);
        assertTrue(patchEngine.getConfidence("add null check") > 0.7);
        assertEquals(0.0, patchEngine.getConfidence("complex refactoring"));
    }

    @Test
    void testDeterministicPatchEngine_GeneratePatch() {
        String javaCode = "public class Test {\n    public void method() {\n    }\n}";
        Optional<String> patch = patchEngine.generatePatch("add logging", javaCode, "Test.java");

        assertTrue(patch.isPresent());
        assertTrue(patch.get().contains("log.info"));
    }

    @Test
    void testAiBudgetTracker_BudgetManagement() {
        String execId = "test-exec-1";
        budgetTracker.initializeBudget(execId, 3);

        assertEquals(3, budgetTracker.getRemainingBudget(execId));
        assertTrue(budgetTracker.canUseAi(execId));

        budgetTracker.consumeAiCall(execId);
        assertEquals(2, budgetTracker.getRemainingBudget(execId));

        budgetTracker.consumeAiCall(execId);
        budgetTracker.consumeAiCall(execId);
        assertEquals(0, budgetTracker.getRemainingBudget(execId));
        assertFalse(budgetTracker.canUseAi(execId));

        budgetTracker.clearBudget(execId);
        assertEquals(0, budgetTracker.getRemainingBudget(execId));
    }

    @Test
    void testAiResponseCache_CacheHitMiss() {
        String task = "add logging";
        String context = "UserService.java";
        String type = "patch";

        Optional<String> result = aiCache.get(task, context, type);
        assertFalse(result.isPresent());
        aiCache.put(task, context, type, "patch content");
        result = aiCache.get(task, context, type);
        assertTrue(result.isPresent());
        assertEquals("patch content", result.get());
    }

    @Test
    void testAiResponseCache_DifferentKeys() {
        aiCache.put("task1", "context1", "type1", "response1");
        aiCache.put("task2", "context2", "type2", "response2");

        assertEquals("response1", aiCache.get("task1", "context1", "type1").get());
        assertEquals("response2", aiCache.get("task2", "context2", "type2").get());
        assertFalse(aiCache.get("task1", "context2", "type1").isPresent());
    }

    @Test
    void testEndToEndEscalation_SimpleTask() {
        String task = "add logging to UserService";

        TaskComplexity complexity = complexityAnalyzer.analyze(task);
        assertEquals(TaskComplexity.SIMPLE, complexity);
        assertFalse(aiPolicy.shouldUseAiPlanning(complexity));
        assertFalse(aiPolicy.shouldUseAiPatching(complexity));
        String execId = "exec-1";
        budgetTracker.initializeBudget(execId, aiPolicy.getMaxAiCalls(complexity));
        assertEquals(1, budgetTracker.getRemainingBudget(execId));
        double confidence = patchEngine.getConfidence(task);
        assertTrue(confidence > 0.7);
        assertEquals(1, budgetTracker.getRemainingBudget(execId));
    }

    @Test
    void testEndToEndEscalation_ComplexTask() {
        String task = "redesign authentication architecture";

        TaskComplexity complexity = complexityAnalyzer.analyze(task);
        assertEquals(TaskComplexity.COMPLEX, complexity);
        assertTrue(aiPolicy.shouldUseAiPlanning(complexity));
        assertTrue(aiPolicy.shouldUseAiPatching(complexity));

        String execId = "exec-2";
        budgetTracker.initializeBudget(execId, aiPolicy.getMaxAiCalls(complexity));
        assertEquals(5, budgetTracker.getRemainingBudget(execId));

        budgetTracker.consumeAiCall(execId); // Planning
        budgetTracker.consumeAiCall(execId); // Patching
        assertEquals(3, budgetTracker.getRemainingBudget(execId));
    }
}
