package com.aiagent.orchestrator.service.impl;

import com.aiagent.orchestrator.service.AiReasoningService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class SpringAiReasoningService implements AiReasoningService {

    private final ChatModel chatModel;

    public SpringAiReasoningService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String ask(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            return "Prompt cannot be empty";
        }

        try {
            return chatModel.call(new Prompt(prompt)).getResult().getOutput().getContent();
        } catch (Exception e) {
            return "Error calling AI model: " + e.getMessage();
        }
    }
}