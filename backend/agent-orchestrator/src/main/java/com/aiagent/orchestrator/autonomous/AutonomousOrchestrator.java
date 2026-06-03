package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import com.aiagent.orchestrator.adaptive.ExecutionRisk;
import com.aiagent.orchestrator.adaptive.ExecutionRiskAnalyzer;
import com.aiagent.orchestrator.service.PlanningService;
import com.aiagent.orchestrator.service.ToolExecutionService;
import com.aiagent.orchestrator.streaming.ExecutionEvent;
import com.aiagent.orchestrator.streaming.ExecutionEventBus;
import com.aiagent.orchestrator.streaming.ExecutionEventType;
import com.aiagent.orchestrator.escalation.*;
import com.aiagent.orchestrator.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AutonomousOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(AutonomousOrchestrator.class);
    private static final int MAX_ATTEMPTS = 10;

    @Autowired
    private NextActionEngine nextActionEngine;

    @Autowired
    private UnifiedToolExecutor toolExecutor;

    @Autowired
    private LearningUpdater learningUpdater;

    @Autowired
    private PlanningService planningService;

    @Autowired
    private ExecutionRiskAnalyzer riskAnalyzer;

    @Autowired(required = false)
    private ExecutionEventBus eventBus;

    @Autowired
    private TaskComplexityAnalyzer complexityAnalyzer;

    @Autowired
    private AiExecutionPolicy aiPolicy;

    @Autowired
    private DeterministicPatchEngine deterministicPatchEngine;

    @Autowired
    private AiBudgetTracker budgetTracker;

    @Autowired
    private AiResponseCache aiCache;

    @Autowired
    private TaskIntentClassifier intentClassifier;

    @Autowired
    private AnalysisExecutor analysisExecutor;

    public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
        String executionId = UUID.randomUUID().toString();

        TaskIntent intent = intentClassifier.classify(request.getTask());
        log.info("Task intent: {}", intent);

        if (intent == TaskIntent.ANALYSIS) {
            return executeAnalysis(request, executionId);
        } else {
            return executeModification(request, executionId);
        }
    }

    private OrchestratorTaskResponse executeAnalysis(OrchestratorTaskRequest request, String executionId) {
        log.info("Executing ANALYSIS mode for task: {}", request.getTask());

        publishEvent(executionId, ExecutionEventType.PLAN_CREATED,
                "Analysis mode: Reading and explaining code");

        AnalysisResult analysis = analysisExecutor.analyze(
                request.getTask(),
                request.getWorkspacePath(),
                request.getSourceFiles()
        );

        List<ExecutionState> completedStates = new ArrayList<>();
        completedStates.add(ExecutionState.CREATED);
        completedStates.add(ExecutionState.COMPLETED);

        publishEvent(executionId, ExecutionEventType.COMPLETE,
                "Analysis completed: " + analysis.getFilesAnalyzed().size() + " files analyzed");

        StringBuilder result = new StringBuilder();
        result.append("FILES_ANALYZED:").append(String.join(", ", analysis.getFilesAnalyzed())).append("\n");
        result.append("KEY_FINDINGS:").append(String.join(", ", analysis.getKeyFindings())).append("\n");
        result.append("EXPLANATION:").append(analysis.getDetailedExplanation());

        OrchestratorTaskResponse response = new OrchestratorTaskResponse(
                executionId,
                completedStates,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        response.setAnalysisResult(result.toString());

        return response;
    }

    private OrchestratorTaskResponse executeModification(OrchestratorTaskRequest request, String executionId) {
        AutonomousExecutionContext context = new AutonomousExecutionContext(executionId, request.getTask());

        if (request.getWorkspacePath() != null) {
            context.setWorkspacePath(request.getWorkspacePath());
            context.setAvailableFiles(request.getSourceFiles());
            log.info("Workspace: {}, Files: {}", request.getWorkspacePath(),
                    request.getSourceFiles() != null ? request.getSourceFiles().size() : 0);
        }

        TaskComplexity complexity = complexityAnalyzer.analyze(request.getTask());
        context.setComplexity(complexity);
        int maxAiCalls = aiPolicy.getMaxAiCalls(complexity);
        budgetTracker.initializeBudget(executionId, maxAiCalls);

        log.info("Task complexity: {}, AI budget: {}", complexity, maxAiCalls);
        publishEvent(executionId, ExecutionEventType.PLAN_CREATED,
                "Complexity: " + complexity + ", AI budget: " + maxAiCalls);

        List<ExecutionState> completedStates = new ArrayList<>();
        completedStates.add(ExecutionState.CREATED);

        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            long startTime = System.currentTimeMillis();

            NextAction action = nextActionEngine.decide(context);
            log.info("Loop iteration {}: action={}, reason={}", attempts, action.getType(), action.getReason());

            publishEvent(executionId, ExecutionEventType.TOOL_SELECTED,
                    "Action selected: " + action.getType() + " - " + action.getReason());

            boolean success = executeAction(action, context, completedStates);

            publishEvent(executionId, ExecutionEventType.TOOL_EXECUTED,
                    "Action executed: " + action.getType() + " - " + (success ? "SUCCESS" : "FAILED"));

            long duration = System.currentTimeMillis() - startTime;

            learningUpdater.update(context, success, duration);

            publishEvent(executionId, ExecutionEventType.LEARNING_UPDATED,
                    "Learning updated from execution");

            if (action.getType() == ActionType.COMPLETE) {
                log.info("Autonomous execution completed after {} iterations", attempts);
                publishEvent(executionId, ExecutionEventType.COMPLETE,
                        "Execution completed successfully after " + attempts + " iterations");
                break;
            }

            if (!success && action.getType() == ActionType.ANALYZE_FAILURE) {
                log.warn("Execution failed after failure analysis");
                context.setCurrentState(ExecutionState.FAILED);
                completedStates.add(ExecutionState.FAILED);
                publishEvent(executionId, ExecutionEventType.ERROR,
                        "Execution failed after failure analysis");
                break;
            }
        }

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Max attempts reached, terminating execution");
            context.setCurrentState(ExecutionState.FAILED);
            completedStates.add(ExecutionState.FAILED);
            publishEvent(executionId, ExecutionEventType.ERROR,
                    "Max attempts reached, execution terminated");
        }

        budgetTracker.clearBudget(executionId);

        return new OrchestratorTaskResponse(
                executionId,
                completedStates,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private boolean executeAction(NextAction action, AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        try {
            switch (action.getType()) {
                case PLAN:
                    return executePlan(context, completedStates);

                case EXECUTE_TOOL:
                    return executeToolAction(context, completedStates);

                case GENERATE_PATCH:
                    return executeGeneratePatch(context, completedStates);

                case APPLY_PATCH:
                    return executeApplyPatch(context, completedStates);

                case RETRY:
                    return executeRetry(context, completedStates);

                case ANALYZE_FAILURE:
                    return executeAnalyzeFailure(context, completedStates);

                case COMPLETE:
                    context.setCurrentState(ExecutionState.COMPLETED);
                    completedStates.add(ExecutionState.COMPLETED);
                    return true;

                default:
                    log.warn("Unknown action type: {}", action.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("Action execution failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean executePlan(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        TaskComplexity complexity = context.getComplexity();
        if (!aiPolicy.shouldUseAiPlanning(complexity)) {
            log.info("Skipping AI planning for SIMPLE task");
            ExecutionPlan simplePlan = new ExecutionPlan();
            PlanStep step = new PlanStep();
            step.setOrder(1);
            step.setDescription("Execute deterministic action");
            simplePlan.setSteps(List.of(step));
            context.setPlan(simplePlan);
            context.setCurrentState(ExecutionState.PLANNING);
            completedStates.add(ExecutionState.PLANNING);
            return true;
        }
        if (!budgetTracker.canUseAi(context.getExecutionId())) {
            log.warn("AI budget exhausted, using fallback planning");
            return false;
        }

        budgetTracker.consumeAiCall(context.getExecutionId());
        ExecutionPlan plan = planningService.createPlan(context.getTask());
        context.setPlan(plan);
        context.setCurrentState(ExecutionState.PLANNING);
        completedStates.add(ExecutionState.PLANNING);

        publishEvent(context.getExecutionId(), ExecutionEventType.PLAN_CREATED,
                "Plan created with " + plan.getSteps().size() + " steps");

        ExecutionRisk risk = riskAnalyzer.analyzeRisk(context.getTask(), "PLANNING");
        context.setRisk(risk);

        publishEvent(context.getExecutionId(), ExecutionEventType.RISK_ANALYZED,
                "Risk score: " + risk.getRiskScore() + " - " + risk.getReason());

        log.info("Plan created with {} steps, risk={}", plan.getSteps().size(), risk.getRiskScore());
        return true;
    }

    private boolean executeToolAction(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        ExecutionState nextState = getNextState(context.getCurrentState());
        context.setCurrentState(nextState);
        completedStates.add(nextState);

        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);
        return result.isSuccess();
    }

    private boolean executeGeneratePatch(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        context.setCurrentState(ExecutionState.VERIFYING);
        if (!completedStates.contains(ExecutionState.VERIFYING)) {
            completedStates.add(ExecutionState.VERIFYING);
        }

        TaskComplexity complexity = context.getComplexity();
        if (complexity == TaskComplexity.SIMPLE) {
            double confidence = deterministicPatchEngine.getConfidence(context.getTask());
            if (confidence > 0.7) {
                log.info("Using deterministic patch engine (confidence: {})", confidence);
                publishEvent(context.getExecutionId(), ExecutionEventType.PATCH_GENERATED,
                        "Deterministic patch generated (confidence: " + confidence + ")");
                return true;
            }
            log.info("Deterministic patching confidence too low ({}), using AI", confidence);
        }
        if (!budgetTracker.canUseAi(context.getExecutionId())) {
            log.warn("AI budget exhausted, cannot generate patch");
            return false;
        }

        budgetTracker.consumeAiCall(context.getExecutionId());
        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);

        if (result.isSuccess() && result.getData() instanceof List) {
            @SuppressWarnings("unchecked")
            List<com.aiagent.common.model.PatchProposal> patches =
                    (List<com.aiagent.common.model.PatchProposal>) result.getData();
            context.addGeneratedPatches(patches);
            log.info("Stored {} patches in execution context", patches.size());
        }

        if (result.isSuccess()) {
            publishEvent(context.getExecutionId(), ExecutionEventType.PATCH_GENERATED,
                    "Patches generated successfully");
            context.setCurrentState(ExecutionState.PATCH_APPLYING);
        }

        return result.isSuccess();
    }

    private boolean executeApplyPatch(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        context.setCurrentState(ExecutionState.PATCH_APPLYING);
        if (!completedStates.contains(ExecutionState.PATCH_APPLYING)) {
            completedStates.add(ExecutionState.PATCH_APPLYING);
        }

        ToolExecutionService.ToolExecutionResult result = toolExecutor.execute(context);

        if (result.isSuccess()) {
            publishEvent(context.getExecutionId(), ExecutionEventType.PATCH_APPLIED,
                    "Patches applied successfully");
            context.setCurrentState(ExecutionState.COMPLETED);
        }

        return result.isSuccess();
    }

    private void publishEvent(String executionId, ExecutionEventType type, String message) {
        if (eventBus != null) {
            ExecutionEvent event = new ExecutionEvent(executionId, type, message);
            eventBus.publish(event);
        }
    }

    private boolean executeRetry(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        log.info("Retrying execution from state: {}", context.getCurrentState());
        // Reset to planning state for retry
        context.setCurrentState(ExecutionState.PLANNING);
        return true;
    }

    private boolean executeAnalyzeFailure(AutonomousExecutionContext context, List<ExecutionState> completedStates) {
        log.warn("Analyzing failure for task: {}", context.getTask());
        return false;
    }

    private ExecutionState getNextState(ExecutionState currentState) {
        switch (currentState) {
            case CREATED:
            case PLANNING:
                return ExecutionState.CONTEXT_LOADING;
            case CONTEXT_LOADING:
                return ExecutionState.EXECUTING;
            case EXECUTING:
                return ExecutionState.VERIFYING;
            default:
                return currentState;
        }
    }
}
