package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiEnhancedPlanningServiceTest {

    private AiEnhancedPlanningService aiEnhancedPlanningService;
    private ExecutionPlan deterministicPlan;

    @BeforeEach
    void setUp() {
        // Create test implementations
        AiPlanningPromptService promptService = new TestPromptService();
        AiReasoningService aiReasoningService = new TestAiReasoningService();
        ObjectMapper objectMapper = new ObjectMapper();
        
        aiEnhancedPlanningService = new AiEnhancedPlanningService(promptService, aiReasoningService, objectMapper);
        
        // Setup deterministic plan
        deterministicPlan = new ExecutionPlan();
        deterministicPlan.setExecutionId("test-plan");
        deterministicPlan.setSteps(List.of(
            new PlanStep(1, "Search repository", ToolType.REPO_SEARCH, "cache"),
            new PlanStep(2, "Generate patch", ToolType.PATCH_GENERATE, "UserService.java")
        ));
    }

    @Test
    void testValidLlmResponseAccepted() {
        // Given
        String task = "Add caching to UserService";
        TestAiReasoningService.setResponse(validJsonResponse());

        // When
        ExecutionPlan result = aiEnhancedPlanningService.enhancePlan(task, deterministicPlan);

        // Then
        assertNotNull(result);
        assertEquals("enhanced-plan", result.getExecutionId());
        assertEquals(3, result.getSteps().size());
        assertEquals("Search for existing cache patterns", result.getSteps().get(0).getDescription());
        assertEquals(ToolType.REPO_SEARCH, result.getSteps().get(0).getToolType());
    }

    @Test
    void testInvalidJsonFallback() {
        // Given
        String task = "Add caching to UserService";
        TestAiReasoningService.setResponse("{ invalid json }");

        // When
        ExecutionPlan result = aiEnhancedPlanningService.enhancePlan(task, deterministicPlan);

        // Then
        assertNotNull(result);
        assertEquals(deterministicPlan.getExecutionId(), result.getExecutionId());
        assertEquals(deterministicPlan.getSteps().size(), result.getSteps().size());
    }

    @Test
    void testNullResponseFallback() {
        // Given
        String task = "Add caching to UserService";
        TestAiReasoningService.setResponse(null);

        // When
        ExecutionPlan result = aiEnhancedPlanningService.enhancePlan(task, deterministicPlan);

        // Then
        assertNotNull(result);
        assertEquals(deterministicPlan.getExecutionId(), result.getExecutionId());
    }

    @Test
    void testInvalidInputFallback() {
        // When & Then
        ExecutionPlan result1 = aiEnhancedPlanningService.enhancePlan(null, deterministicPlan);
        assertEquals(deterministicPlan, result1);

        ExecutionPlan result2 = aiEnhancedPlanningService.enhancePlan("", deterministicPlan);
        assertEquals(deterministicPlan, result2);

        ExecutionPlan result3 = aiEnhancedPlanningService.enhancePlan("task", null);
        assertNull(result3);
    }

    private String validJsonResponse() {
        return """
            {
                "executionId": "enhanced-plan",
                "steps": [
                    {
                        "order": 1,
                        "description": "Search for existing cache patterns",
                        "toolType": "REPO_SEARCH",
                        "target": "cache"
                    },
                    {
                        "order": 2,
                        "description": "Load UserService context",
                        "toolType": "CONTEXT_LOAD",
                        "target": "UserService.java"
                    },
                    {
                        "order": 3,
                        "description": "Generate cache implementation",
                        "toolType": "PATCH_GENERATE",
                        "target": "UserService.java"
                    }
                ]
            }
            """;
    }

    // Test implementations
    static class TestPromptService extends AiPlanningPromptService {
        public TestPromptService() {
            super(new ObjectMapper());
        }
    }

    static class TestAiReasoningService implements AiReasoningService {
        private static String response = "test response";
        
        public static void setResponse(String response) {
            TestAiReasoningService.response = response;
        }
        
        @Override
        public String ask(String prompt) {
            return response;
        }
    }
}