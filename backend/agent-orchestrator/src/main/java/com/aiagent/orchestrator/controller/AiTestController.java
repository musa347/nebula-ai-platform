package com.aiagent.orchestrator.controller;

import com.aiagent.orchestrator.dto.AiRequest;
import com.aiagent.orchestrator.dto.AiResponse;
import com.aiagent.orchestrator.service.AiReasoningService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiTestController {

    private final AiReasoningService aiReasoningService;

    public AiTestController(AiReasoningService aiReasoningService) {
        this.aiReasoningService = aiReasoningService;
    }

    @PostMapping("/ask")
    public AiResponse ask(@RequestBody AiRequest request) {

        String response = aiReasoningService.ask(request.getPrompt());

        return new AiResponse(response);
    }
}