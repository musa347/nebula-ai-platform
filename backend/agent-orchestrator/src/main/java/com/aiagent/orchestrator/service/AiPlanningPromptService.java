package com.aiagent.orchestrator.service;

import com.aiagent.common.model.ExecutionPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AiPlanningPromptService {

    private final ObjectMapper objectMapper;

    public AiPlanningPromptService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildPlanningPrompt(String task, ExecutionPlan plan) {
        
        if (task == null || task.isBlank()) {
            throw new IllegalArgumentException("Task cannot be null or empty");
        }
        
        if (plan == null) {
            throw new IllegalArgumentException("Plan cannot be null");
        }

        String planJson;
        try {
            planJson = objectMapper.writeValueAsString(plan);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize plan to JSON", e);
        }

        return buildPromptTemplate(task, planJson);
    }

    private String buildPromptTemplate(String task, String planJson) {
        return String.format("""
            You are a software planning assistant.
            
            TASK:
            %s
            
            DETERMINISTIC PLAN:
            %s
            
            RULES:
            - Do NOT remove required steps
            - Do NOT change execution safety steps
            - You MAY reorder non-critical steps
            - You MAY suggest better sequencing
            - Output ONLY valid JSON plan
            """, task, planJson);
    }
}