package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiEnhancedPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(AiEnhancedPlanningService.class);

    private final AiPlanningPromptService promptService;
    private final AiReasoningService aiReasoningService;
    private final ObjectMapper objectMapper;

    public AiEnhancedPlanningService(
            AiPlanningPromptService promptService,
            AiReasoningService aiReasoningService,
            ObjectMapper objectMapper) {
        this.promptService = promptService;
        this.aiReasoningService = aiReasoningService;
        this.objectMapper = objectMapper;
    }

    public ExecutionPlan enhancePlan(String task, ExecutionPlan deterministicPlan) {
        
        if (task == null || task.isBlank() || deterministicPlan == null) {
            logger.warn("Invalid input for plan enhancement, returning deterministic plan");
            return deterministicPlan;
        }

        try {
            // Generate prompt
            String prompt = promptService.buildPlanningPrompt(task, deterministicPlan);
            
            // Call AI service
            String aiResponse = aiReasoningService.ask(prompt);
            
            // Parse and validate response
            ExecutionPlan enhancedPlan = parseAndValidateResponse(aiResponse, deterministicPlan);
            
            if (enhancedPlan != null) {
                logger.info("Successfully enhanced plan for task: {}", task);
                return enhancedPlan;
            }
            
        } catch (Exception e) {
            logger.warn("AI plan enhancement failed for task: {}, falling back to deterministic plan. Error: {}", 
                       task, e.getMessage());
        }
        
        // Fallback to deterministic plan
        logger.info("Using deterministic plan for task: {}", task);
        return deterministicPlan;
    }

    private ExecutionPlan parseAndValidateResponse(String aiResponse, ExecutionPlan fallbackPlan) {
        
        if (aiResponse == null || aiResponse.isBlank()) {
            logger.warn("Empty AI response, using fallback plan");
            return null;
        }

        try {
            // Parse JSON
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            
            // Validate structure
            if (!rootNode.has("executionId") || !rootNode.has("steps")) {
                logger.warn("Invalid JSON structure, missing required fields");
                return null;
            }
            
            JsonNode stepsNode = rootNode.get("steps");
            if (!stepsNode.isArray() || stepsNode.size() == 0) {
                logger.warn("Invalid steps array in AI response");
                return null;
            }
            
            // Parse steps
            List<PlanStep> steps = new ArrayList<>();
            for (JsonNode stepNode : stepsNode) {
                PlanStep step = parseStep(stepNode);
                if (step == null) {
                    logger.warn("Invalid step in AI response, using fallback plan");
                    return null;
                }
                steps.add(step);
            }
            
            // Create enhanced plan
            ExecutionPlan enhancedPlan = new ExecutionPlan();
            enhancedPlan.setExecutionId(rootNode.get("executionId").asText());
            enhancedPlan.setSteps(steps);
            
            return enhancedPlan;
            
        } catch (Exception e) {
            logger.warn("Failed to parse AI response: {}", e.getMessage());
            return null;
        }
    }

    private PlanStep parseStep(JsonNode stepNode) {
        
        if (!stepNode.has("order") || !stepNode.has("description") || 
            !stepNode.has("toolType") || !stepNode.has("target")) {
            return null;
        }
        
        try {
            int order = stepNode.get("order").asInt();
            String description = stepNode.get("description").asText();
            String toolTypeStr = stepNode.get("toolType").asText();
            String target = stepNode.get("target").asText();
            
            // Validate tool type
            ToolType toolType;
            try {
                toolType = ToolType.valueOf(toolTypeStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown tool type: {}", toolTypeStr);
                return null;
            }
            
            return new PlanStep(order, description, toolType, target);
            
        } catch (Exception e) {
            logger.warn("Failed to parse step: {}", e.getMessage());
            return null;
        }
    }
}