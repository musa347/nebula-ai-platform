package com.aiagent.common.dto;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import java.util.List;

public class OrchestratorTaskResponse {
    private String executionId;
    private List<ExecutionState> completedStates;
    private List<LoadedContext> contexts;
    private List<FilePreview> previews;
    private List<PatchProposal> patches;
    private List<PatchExecutionResult> patchResults;
    private String analysisResult;

    public OrchestratorTaskResponse() {}

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates) {
        this.executionId = executionId;
        this.completedStates = completedStates;
    }

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts) {
        this.executionId = executionId;
        this.completedStates = completedStates;
        this.contexts = contexts;
    }

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts, List<FilePreview> previews) {
        this.executionId = executionId;
        this.completedStates = completedStates;
        this.contexts = contexts;
        this.previews = previews;
    }

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts, List<FilePreview> previews, List<PatchProposal> patches) {
        this.executionId = executionId;
        this.completedStates = completedStates;
        this.contexts = contexts;
        this.previews = previews;
        this.patches = patches;
    }

    public OrchestratorTaskResponse(String executionId, List<ExecutionState> completedStates, List<LoadedContext> contexts, List<FilePreview> previews, List<PatchProposal> patches, List<PatchExecutionResult> patchResults) {
        this.executionId = executionId;
        this.completedStates = completedStates;
        this.contexts = contexts;
        this.previews = previews;
        this.patches = patches;
        this.patchResults = patchResults;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public List<ExecutionState> getCompletedStates() {
        return completedStates;
    }

    public void setCompletedStates(List<ExecutionState> completedStates) {
        this.completedStates = completedStates;
    }

    public List<LoadedContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<LoadedContext> contexts) {
        this.contexts = contexts;
    }

    public List<FilePreview> getPreviews() {
        return previews;
    }

    public void setPreviews(List<FilePreview> previews) {
        this.previews = previews;
    }

    public List<PatchProposal> getPatches() {
        return patches;
    }

    public void setPatches(List<PatchProposal> patches) {
        this.patches = patches;
    }

    public List<PatchExecutionResult> getPatchResults() {
        return patchResults;
    }

    public void setPatchResults(List<PatchExecutionResult> patchResults) {
        this.patchResults = patchResults;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }
}