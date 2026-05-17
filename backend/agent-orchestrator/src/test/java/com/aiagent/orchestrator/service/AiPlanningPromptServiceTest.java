package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiPlanningPromptServiceTest {

    private AiPlanningPromptService promptService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptService = new AiPlanningPromptService(objectMapper);
    }

    @Test
    void testBuildPlanningPrompt() {
        // Given
        String task = "Implement Redis caching";
        ExecutionPlan plan = new ExecutionPlan();
        plan.setExecutionId("test-task");
        plan.setSteps(List.of(
            new PlanStep(1, "search for cache implementations", ToolType.REPO_SEARCH, "cache"),
            new PlanStep(2, "create cache configuration", ToolType.PATCH_GENERATE, "config")
        ));

        // When
        String prompt = promptService.buildPlanningPrompt(task, plan);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Implement Redis caching"));
        assertTrue(prompt.contains("DETERMINISTIC PLAN:"));
        assertTrue(prompt.contains("REPO_SEARCH"));
        assertTrue(prompt.contains("PATCH_GENERATE"));
        assertTrue(prompt.contains("Do NOT remove required steps"));
        assertTrue(prompt.contains("Output ONLY valid JSON plan"));
    }

    @Test
    void testBuildPlanningPromptWithNullTask() {
        // Given
        ExecutionPlan plan = new ExecutionPlan();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            promptService.buildPlanningPrompt(null, plan));
    }

    @Test
    void testBuildPlanningPromptWithEmptyTask() {
        // Given
        ExecutionPlan plan = new ExecutionPlan();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            promptService.buildPlanningPrompt("", plan));
    }

    @Test
    void testBuildPlanningPromptWithNullPlan() {
        // Given
        String task = "Test task";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            promptService.buildPlanningPrompt(task, null));
    }

    @Test
    void testPromptContainsNoNullValues() {
        // Given
        String task = "Test task";
        ExecutionPlan plan = new ExecutionPlan();
        plan.setExecutionId("test");
        plan.setSteps(List.of());

        // When
        String prompt = promptService.buildPlanningPrompt(task, plan);

        // Then
        assertNotNull(prompt);
        assertFalse(prompt.contains("null"));
        assertTrue(prompt.length() > 0);
    }
}