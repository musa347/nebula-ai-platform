package com.aiagent.orchestrator.service.impl;

import com.aiagent.orchestrator.service.AiReasoningService;
import com.aiagent.orchestrator.service.HybridAiService;
import org.springframework.stereotype.Service;

@Service
public class SpringAiReasoningService implements AiReasoningService {

    private final HybridAiService hybridAiService;

    public SpringAiReasoningService(HybridAiService hybridAiService) {
        this.hybridAiService = hybridAiService;
    }

    @Override
    public String ask(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            return "Prompt cannot be empty";
        }

        try {
            return hybridAiService.callWithFallback(prompt);
        } catch (Exception e) {
            return "Error calling AI model: " + e.getMessage();
        }
    }
}