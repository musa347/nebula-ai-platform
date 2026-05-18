package com.aiagent.orchestrator.retry;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.common.enums.FailureType;
import com.aiagent.orchestrator.actions.EmbabelActions;
import com.aiagent.orchestrator.dto.AiFailureRequest;
import com.aiagent.orchestrator.dto.AiFailureResponse;
import com.aiagent.orchestrator.model.RecoveryAction;
import com.aiagent.orchestrator.model.RecoveryStrategy;
import com.aiagent.orchestrator.service.AiFailureAnalysisService;
import com.aiagent.orchestrator.service.ExecutionGraphService;
import com.aiagent.orchestrator.service.RecoveryStrategyService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RetryOrchestratorIntegrationTest {

    @Test
    void compileFailureRetryGenerated() {
        EmbabelActions actions = mock(EmbabelActions.class);
        FailureClassifier classifier = mock(FailureClassifier.class);
        ExecutionGraphService graphService = mock(ExecutionGraphService.class);
        AiFailureAnalysisService aiService = mock(AiFailureAnalysisService.class);
        RecoveryStrategyService recoveryService = mock(RecoveryStrategyService.class);

        when(actions.executeShell(any())).thenReturn("compilation error");
        when(classifier.classify(any())).thenReturn(FailureType.COMPILATION_FAILURE);

        AiFailureResponse aiResponse = new AiFailureResponse();
        aiResponse.setFailureType("COMPILATION_ERROR");
        aiResponse.setRootCause("Missing semicolon");
        aiResponse.setSuggestedRecovery("Regenerate patch");
        when(aiService.analyzeFailure(any(AiFailureRequest.class))).thenReturn(aiResponse);

        RecoveryStrategy strategy = new RecoveryStrategy();
        strategy.setMaxRetries(2);
        strategy.setActions(List.of(RecoveryAction.REGENERATE_PATCH, RecoveryAction.RETRY_EXECUTION));
        when(recoveryService.buildStrategy(any())).thenReturn(strategy);

        RetryOrchestrator orchestrator = new RetryOrchestrator(
                actions, classifier, graphService, aiService, recoveryService, true);

        RetryPolicy policy = new RetryPolicy.Builder()
                .maxRetries(1)
                .retryDelayMs(100)
                .maxExecutionDurationMs(5000)
                .build();
        Flux<ExecutionEvent> events = orchestrator.executeWithRetry("mvn compile", policy);

        StepVerifier.create(events)
                .expectNextMatches(e -> e.getMessage().contains("State"))
                .expectComplete()
                .verify(Duration.ofSeconds(2));

        verify(aiService, atLeastOnce()).analyzeFailure(any());
        verify(recoveryService, atLeastOnce()).buildStrategy(any());
    }

    @Test
    void invalidAiResponseDeterministicFallback() {
        EmbabelActions actions = mock(EmbabelActions.class);
        FailureClassifier classifier = mock(FailureClassifier.class);
        ExecutionGraphService graphService = mock(ExecutionGraphService.class);
        AiFailureAnalysisService aiService = mock(AiFailureAnalysisService.class);
        RecoveryStrategyService recoveryService = mock(RecoveryStrategyService.class);

        when(actions.executeShell(any())).thenReturn("error");
        when(classifier.classify(any())).thenReturn(FailureType.UNKNOWN);
        when(aiService.analyzeFailure(any())).thenThrow(new RuntimeException("AI failed"));

        RetryOrchestrator orchestrator = new RetryOrchestrator(
                actions, classifier, graphService, aiService, recoveryService, true);

        RetryPolicy policy = new RetryPolicy.Builder()
                .maxRetries(1)
                .retryDelayMs(100)
                .maxExecutionDurationMs(5000)
                .build();
        Flux<ExecutionEvent> events = orchestrator.executeWithRetry("test", policy);

        StepVerifier.create(events)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofSeconds(2));

        verify(classifier, atLeastOnce()).classify(any());
    }

    @Test
    void dangerousRecoverySuggestionRejected() {
        EmbabelActions actions = mock(EmbabelActions.class);
        FailureClassifier classifier = mock(FailureClassifier.class);
        ExecutionGraphService graphService = mock(ExecutionGraphService.class);
        AiFailureAnalysisService aiService = mock(AiFailureAnalysisService.class);
        RecoveryStrategyService recoveryService = mock(RecoveryStrategyService.class);

        when(actions.executeShell(any())).thenReturn("error");
        when(classifier.classify(any())).thenReturn(FailureType.UNKNOWN);

        AiFailureResponse dangerousResponse = new AiFailureResponse();
        dangerousResponse.setFailureType("ERROR");
        dangerousResponse.setRootCause("Issue");
        dangerousResponse.setSuggestedRecovery("Run sudo rm -rf /tmp");
        when(aiService.analyzeFailure(any())).thenReturn(dangerousResponse);

        RetryOrchestrator orchestrator = new RetryOrchestrator(
                actions, classifier, graphService, aiService, recoveryService, true);

        RetryPolicy policy = new RetryPolicy.Builder()
                .maxRetries(0)
                .retryDelayMs(100)
                .maxExecutionDurationMs(5000)
                .build();
        Flux<ExecutionEvent> events = orchestrator.executeWithRetry("test", policy);

        StepVerifier.create(events)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void retryLimitStillEnforced() {
        EmbabelActions actions = mock(EmbabelActions.class);
        FailureClassifier classifier = mock(FailureClassifier.class);
        ExecutionGraphService graphService = mock(ExecutionGraphService.class);
        AiFailureAnalysisService aiService = mock(AiFailureAnalysisService.class);
        RecoveryStrategyService recoveryService = mock(RecoveryStrategyService.class);

        when(actions.executeShell(any())).thenReturn("error");
        when(classifier.classify(any())).thenReturn(FailureType.COMPILATION_FAILURE);

        AiFailureResponse aiResponse = new AiFailureResponse();
        aiResponse.setFailureType("COMPILATION_ERROR");
        aiResponse.setRootCause("Error");
        aiResponse.setSuggestedRecovery("Retry");
        when(aiService.analyzeFailure(any())).thenReturn(aiResponse);

        RecoveryStrategy strategy = new RecoveryStrategy();
        strategy.setMaxRetries(10);
        strategy.setActions(List.of(RecoveryAction.RETRY_EXECUTION));
        when(recoveryService.buildStrategy(any())).thenReturn(strategy);

        RetryOrchestrator orchestrator = new RetryOrchestrator(
                actions, classifier, graphService, aiService, recoveryService, true);

        RetryPolicy policy = new RetryPolicy.Builder()
                .maxRetries(2)
                .retryDelayMs(50)
                .maxExecutionDurationMs(5000)
                .build();
        Flux<ExecutionEvent> events = orchestrator.executeWithRetry("test", policy);

        StepVerifier.create(events)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofSeconds(3));

        verify(actions, atMost(3)).executeShell(any());
    }
}
