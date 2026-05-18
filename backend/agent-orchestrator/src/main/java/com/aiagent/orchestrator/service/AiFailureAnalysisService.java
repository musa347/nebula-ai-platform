package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiFailureRequest;
import com.aiagent.orchestrator.dto.AiFailureResponse;
import com.aiagent.orchestrator.retry.FailureClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AiFailureAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AiFailureAnalysisService.class);
    
    private final ChatModel chatModel;
    private final AiFailurePromptService promptService;
    private final FailureClassifier fallbackClassifier;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    
    private static final List<Pattern> DANGEROUS_PATTERNS = List.of(
        Pattern.compile("rm\\s+-rf", Pattern.CASE_INSENSITIVE),
        Pattern.compile("sudo\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("chmod\\s+777", Pattern.CASE_INSENSITIVE),
        Pattern.compile("curl.*\\|.*sh", Pattern.CASE_INSENSITIVE),
        Pattern.compile("wget.*\\|.*sh", Pattern.CASE_INSENSITIVE),
        Pattern.compile("exec\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("npm\\s+install\\s+-g", Pattern.CASE_INSENSITIVE),
        Pattern.compile("pip\\s+install", Pattern.CASE_INSENSITIVE),
        Pattern.compile("apt-get\\s+install", Pattern.CASE_INSENSITIVE),
        Pattern.compile("yum\\s+install", Pattern.CASE_INSENSITIVE)
    );
    
    public AiFailureAnalysisService(
            ChatModel chatModel,
            AiFailurePromptService promptService,
            FailureClassifier fallbackClassifier,
            ObjectMapper objectMapper,
            @Value("${ai.failure-analysis.enabled:true}") boolean enabled) {
        this.chatModel = chatModel;
        this.promptService = promptService;
        this.fallbackClassifier = fallbackClassifier;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }
    
    public AiFailureResponse analyzeFailure(AiFailureRequest request) {
        if (!enabled) {
            log.info("AI failure analysis disabled, using fallback");
            return useFallback(request);
        }
        
        try {
            String promptText = promptService.buildFailurePrompt(request);
            Prompt prompt = new Prompt(promptText);
            
            String response = chatModel.call(prompt).getResult().getOutput().getContent();
            log.debug("AI failure analysis response: {}", response);
            
            AiFailureResponse analysis = parseResponse(response);
            
            if (!isValid(analysis)) {
                log.warn("Invalid AI failure analysis, using fallback");
                return useFallback(request);
            }
            
            if (containsDangerousRecovery(analysis)) {
                log.warn("Dangerous recovery suggestion detected, using fallback");
                return useFallback(request);
            }
            
            log.info("AI failure analysis successful: type={}", analysis.getFailureType());
            return analysis;
            
        } catch (Exception e) {
            log.error("AI failure analysis failed: {}", e.getMessage(), e);
            return useFallback(request);
        }
    }
    
    private AiFailureResponse parseResponse(String response) throws Exception {
        String json = extractJson(response);
        return objectMapper.readValue(json, AiFailureResponse.class);
    }
    
    private String extractJson(String response) {
        if (response.contains("{")) {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            return response.substring(start, end);
        }
        return response;
    }
    
    private boolean isValid(AiFailureResponse response) {
        return response != null
            && response.getFailureType() != null && !response.getFailureType().isBlank()
            && response.getRootCause() != null && !response.getRootCause().isBlank()
            && response.getSuggestedRecovery() != null && !response.getSuggestedRecovery().isBlank();
    }
    
    private boolean containsDangerousRecovery(AiFailureResponse response) {
        String recovery = response.getSuggestedRecovery().toLowerCase();
        return DANGEROUS_PATTERNS.stream()
            .anyMatch(pattern -> pattern.matcher(recovery).find());
    }
    
    private AiFailureResponse useFallback(AiFailureRequest request) {
        String failureType = fallbackClassifier.classify(request.getStderr()).name();
        
        AiFailureResponse response = new AiFailureResponse();
        response.setFailureType(failureType);
        response.setRootCause("Deterministic classification: " + failureType);
        response.setSuggestedRecovery("Retry with regenerated patch");
        
        return response;
    }
}
