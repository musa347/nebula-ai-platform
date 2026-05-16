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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinimalOrchestratorService {

    @Autowired
    private ExecutionSessionService executionSessionService;
    
    @Autowired
    private ContextLoaderService contextLoaderService;
    
    @Autowired
    private FileReaderService fileReaderService;
    
    @Autowired
    private PatchProposalService patchProposalService;
    
    @Autowired
    private PatchExecutionService patchExecutionService;

    public OrchestratorTaskResponse execute(OrchestratorTaskRequest request) {
        List<ExecutionState> completedStates = new ArrayList<>();
        List<LoadedContext> contexts = new ArrayList<>();
        List<FilePreview> previews = new ArrayList<>();
        List<PatchProposal> patches = new ArrayList<>();
        List<PatchExecutionResult> patchResults = new ArrayList<>();
        
        // Step 1: Create session (CREATED state)
        ExecutionSession session = executionSessionService.create();
        completedStates.add(ExecutionState.CREATED);
        
        // Step 2: Transition to PLANNING
        TransitionResult result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.PLANNING);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.PLANNING);
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        // Step 3: Transition to CONTEXT_LOADING
        result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.CONTEXT_LOADING);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.CONTEXT_LOADING);
            // Load context during CONTEXT_LOADING state
            contexts = contextLoaderService.loadContext(request.getTask());
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        // Step 4: Transition to EXECUTING
        result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.EXECUTING);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.EXECUTING);
            // Read file previews during EXECUTING state
            previews = fileReaderService.readFilePreviews(contexts);
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        // Step 5: Transition to VERIFYING
        result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.VERIFYING);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.VERIFYING);
            // Generate patch proposals during VERIFYING state
            patches = patchProposalService.generatePatches(request.getTask(), contexts, previews);
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        // Step 6: Transition to PATCH_APPLYING
        result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.PATCH_APPLYING);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.PATCH_APPLYING);
            // Execute patches during PATCH_APPLYING state
            patchResults = patchExecutionService.executePatches(patches);
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        // Step 7: Transition to COMPLETED
        result = executionSessionService.updateState(session.getExecutionId(), ExecutionState.COMPLETED);
        if (result.isAllowed()) {
            completedStates.add(ExecutionState.COMPLETED);
        } else {
            return handleFailure(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
        }
        
        return new OrchestratorTaskResponse(session.getExecutionId(), completedStates, contexts, previews, patches, patchResults);
    }
    
    private OrchestratorTaskResponse handleFailure(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts, List<FilePreview> previews, List<PatchProposal> patches, List<PatchExecutionResult> patchResults) {
        executionSessionService.updateState(executionId, ExecutionState.FAILED);
        completedStates.add(ExecutionState.FAILED);
        return new OrchestratorTaskResponse(executionId, completedStates, contexts, previews, patches, patchResults);
    }
}