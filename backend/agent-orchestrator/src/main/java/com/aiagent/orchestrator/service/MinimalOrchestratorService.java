package com.aiagent.orchestrator.service;

import com.aiagent.common.dto.OrchestratorTaskRequest;
import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionSession;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import com.aiagent.common.model.TransitionResult;
import com.aiagent.common.model.ToolDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinimalOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(MinimalOrchestratorService.class);

    @Autowired
    private ExecutionSessionService executionSessionService;
    
    @Autowired
    private ToolRouterService toolRouterService;
    
    @Autowired
    private ToolExecutionService toolExecutionService;

    public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
        List<ExecutionState> completedStates = new ArrayList<>();
        List<LoadedContext> contexts = new ArrayList<>();
        List<FilePreview> previews = new ArrayList<>();
        List<PatchProposal> patches = new ArrayList<>();
        List<PatchExecutionResult> patchResults = new ArrayList<>();
        
        // Step 1: Create session (CREATED state)
        ExecutionSession session = executionSessionService.create();
        completedStates.add(ExecutionState.CREATED);
        
        // NEW EXECUTION MODEL: Dynamic tool routing per state
        ExecutionState currentState = ExecutionState.CREATED;
        
        // Execution loop: while not COMPLETED
        while (currentState != ExecutionState.COMPLETED && currentState != ExecutionState.FAILED) {
            // Get next state first
            ExecutionState nextState = getNextState(currentState);
            
            // Transition to next state
            TransitionResult result = executionSessionService.updateState(session.getExecutionId(), nextState);
            
            if (!result.isAllowed()) {
                log.error("State transition failed from {} to {}", currentState, nextState);
                return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
            }
            
            completedStates.add(nextState);
            currentState = nextState;
            
            // Skip tool execution for COMPLETED state
            if (currentState == ExecutionState.COMPLETED) {
                break;
            }
            
            // 1. Decide tool
            ToolDecision toolDecision = toolRouterService.decide(currentState, request.getTask(), 
                contexts.isEmpty() ? null : "Available contexts: " + contexts.size());
            
            log.info("State {}: Selected tool {} - {}", currentState, toolDecision.getToolType(), toolDecision.getReason());
            
            // 2. Execute tool
            ToolExecutionService.ToolExecutionResult toolResult = toolExecutionService.execute(
                toolDecision, request.getTask(), request.getTargetFile());
            
            if (!toolResult.isSuccess()) {
                log.warn("Tool execution failed in state {}: {}", currentState, toolResult.getMessage());
                return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
            }
            
            // 3. Store result based on tool type and current state
            storeToolResult(toolResult, currentState, contexts, previews, patches, patchResults);
        }
        
        return new OrchestratorTaskResponse(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
    }
    
    @SuppressWarnings("unchecked")
    private void storeToolResult(ToolExecutionService.ToolExecutionResult toolResult, ExecutionState currentState, 
                                List<LoadedContext> contexts, List<FilePreview> previews, 
                                List<PatchProposal> patches, List<PatchExecutionResult> patchResults) {
        
        Object data = toolResult.getData();
        if (data == null) {
            return;
        }
        
        switch (currentState) {
            case PLANNING:
            case CONTEXT_LOADING:
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof LoadedContext) {
                        contexts.addAll((List<LoadedContext>) data);
                    }
                }
                break;
                
            case EXECUTING:
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof FilePreview) {
                        previews.addAll((List<FilePreview>) data);
                    }
                }
                break;
                
            case VERIFYING:
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof PatchProposal) {
                        patches.addAll((List<PatchProposal>) data);
                    }
                }
                break;
                
            case PATCH_APPLYING:
                if (data instanceof List) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof PatchExecutionResult) {
                        patchResults.addAll((List<PatchExecutionResult>) data);
                    }
                }
                break;
        }
    }
    
    private ExecutionState getNextState(ExecutionState currentState) {
        switch (currentState) {
            case CREATED:
                return ExecutionState.PLANNING;
            case PLANNING:
                return ExecutionState.CONTEXT_LOADING;
            case CONTEXT_LOADING:
                return ExecutionState.EXECUTING;
            case EXECUTING:
                return ExecutionState.VERIFYING;
            case VERIFYING:
                return ExecutionState.PATCH_APPLYING;
            case PATCH_APPLYING:
                return ExecutionState.COMPLETED;
            default:
                return ExecutionState.FAILED;
        }
    }
    
    private OrchestratorTaskResponse handleFailure(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts, List<FilePreview> previews, List<PatchProposal> patches, List<PatchExecutionResult> patchResults) {
        executionSessionService.updateState(executionId, ExecutionState.FAILED);
        completedStates.add(ExecutionState.FAILED);
        return new OrchestratorTaskResponse(executionId, completedStates, contexts, previews, patches, patchResults);
    }
}