package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.FailureType;
import com.aiagent.orchestrator.dto.AiFailureRequest;
import com.aiagent.orchestrator.dto.AiFailureResponse;
import com.aiagent.orchestrator.retry.FailureClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiFailureAnalysisServiceTest {
    
    @Test
    void validAnalysisAccepted() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        AiFailurePromptService promptService = mock(AiFailurePromptService.class);
        FailureClassifier fallbackClassifier = mock(FailureClassifier.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(promptService.buildFailurePrompt(any())).thenReturn("prompt");
        
        String validJson = "{\"failureType\":\"COMPILATION_ERROR\",\"rootCause\":\"Missing semicolon\",\"suggestedRecovery\":\"Regenerate patch with proper syntax\"}";
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class, RETURNS_DEEP_STUBS);
        when(generation.getOutput().getContent()).thenReturn(validJson);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        AiFailureAnalysisService service = new AiFailureAnalysisService(chatModel, promptService, fallbackClassifier, objectMapper, true);
        
        AiFailureRequest request = new AiFailureRequest();
        request.setStderr("error: ';' expected");
        
        AiFailureResponse response = service.analyzeFailure(request);
        
        assertEquals("COMPILATION_ERROR", response.getFailureType());
        assertEquals("Missing semicolon", response.getRootCause());
        assertNotNull(response.getSuggestedRecovery());
    }
    
    @Test
    void invalidJsonFallback() {
        ChatModel chatModel = mock(ChatModel.class);
        AiFailurePromptService promptService = mock(AiFailurePromptService.class);
        FailureClassifier fallbackClassifier = mock(FailureClassifier.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(promptService.buildFailurePrompt(any())).thenReturn("prompt");
        when(fallbackClassifier.classify(any())).thenReturn(FailureType.UNKNOWN);
        
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class, RETURNS_DEEP_STUBS);
        when(generation.getOutput().getContent()).thenReturn("invalid json");
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        AiFailureAnalysisService service = new AiFailureAnalysisService(chatModel, promptService, fallbackClassifier, objectMapper, true);
        
        AiFailureRequest request = new AiFailureRequest();
        AiFailureResponse response = service.analyzeFailure(request);
        
        assertNotNull(response);
        assertEquals("UNKNOWN", response.getFailureType());
    }
    
    @Test
    void dangerousRecoveryRejected() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        AiFailurePromptService promptService = mock(AiFailurePromptService.class);
        FailureClassifier fallbackClassifier = mock(FailureClassifier.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(promptService.buildFailurePrompt(any())).thenReturn("prompt");
        when(fallbackClassifier.classify(any())).thenReturn(FailureType.UNKNOWN);
        
        String dangerousJson = "{\"failureType\":\"ERROR\",\"rootCause\":\"Issue\",\"suggestedRecovery\":\"Run sudo rm -rf /tmp\"}";
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class, RETURNS_DEEP_STUBS);
        when(generation.getOutput().getContent()).thenReturn(dangerousJson);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        AiFailureAnalysisService service = new AiFailureAnalysisService(chatModel, promptService, fallbackClassifier, objectMapper, true);
        
        AiFailureRequest request = new AiFailureRequest();
        AiFailureResponse response = service.analyzeFailure(request);
        
        assertEquals("UNKNOWN", response.getFailureType());
        assertFalse(response.getSuggestedRecovery().contains("sudo"));
    }
    
    @Test
    void emptyResponseFallback() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        AiFailurePromptService promptService = mock(AiFailurePromptService.class);
        FailureClassifier fallbackClassifier = mock(FailureClassifier.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(promptService.buildFailurePrompt(any())).thenReturn("prompt");
        when(fallbackClassifier.classify(any())).thenReturn(FailureType.UNKNOWN);
        
        String emptyJson = "{\"failureType\":\"\",\"rootCause\":\"\",\"suggestedRecovery\":\"\"}";
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class, RETURNS_DEEP_STUBS);
        when(generation.getOutput().getContent()).thenReturn(emptyJson);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        
        AiFailureAnalysisService service = new AiFailureAnalysisService(chatModel, promptService, fallbackClassifier, objectMapper, true);
        
        AiFailureRequest request = new AiFailureRequest();
        AiFailureResponse response = service.analyzeFailure(request);
        
        assertEquals("UNKNOWN", response.getFailureType());
    }
}
