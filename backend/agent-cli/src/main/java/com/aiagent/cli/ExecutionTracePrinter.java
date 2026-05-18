package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.PatchProposal;
import org.springframework.stereotype.Service;

@Service
public class ExecutionTracePrinter {
    
    public void print(OrchestratorTaskResponse response) {
        printPlan(response);
        printToolSelection(response);
        printContexts(response);
        printExecution(response);
        printPatches(response);
    }
    
    private void printPlan(OrchestratorTaskResponse response) {
        System.out.println("[PLAN]");
        
        if (response.getCompletedStates().contains(ExecutionState.PLANNING)) {
            System.out.println("✓ Planning completed");
            System.out.println("  States to execute: " + response.getCompletedStates().size());
        } else {
            System.out.println("✗ Planning not completed");
        }
        
        System.out.println();
    }
    
    private void printToolSelection(OrchestratorTaskResponse response) {
        System.out.println("[TOOL SELECTION]");
        
        for (ExecutionState state : response.getCompletedStates()) {
            String tool = mapStateToTool(state);
            if (tool != null) {
                System.out.println("  " + state + " → " + tool);
            }
        }
        
        System.out.println();
    }
    
    private void printContexts(OrchestratorTaskResponse response) {
        System.out.println("[MEMORY MATCHES]");
        
        if (response.getContexts() != null && !response.getContexts().isEmpty()) {
            System.out.println("  Loaded " + response.getContexts().size() + " contexts:");
            for (LoadedContext context : response.getContexts()) {
                System.out.println("    - " + context.getFile() + " (" + context.getReason() + ")");
            }
        } else {
            System.out.println("  No contexts loaded");
        }
        
        System.out.println();
    }
    
    private void printExecution(OrchestratorTaskResponse response) {
        System.out.println("[EXECUTION STEPS]");
        
        if (response.getPreviews() != null && !response.getPreviews().isEmpty()) {
            System.out.println("  Read " + response.getPreviews().size() + " files:");
            for (FilePreview preview : response.getPreviews()) {
                int lines = preview.getPreview() != null ? preview.getPreview().split("\n").length : 0;
                System.out.println("    - " + preview.getFile() + " (" + lines + " lines)");
            }
        } else {
            System.out.println("  No files read");
        }
        
        System.out.println();
    }
    
    private void printPatches(OrchestratorTaskResponse response) {
        System.out.println("[PATCHES]");
        
        if (response.getPatches() != null && !response.getPatches().isEmpty()) {
            System.out.println("  Generated " + response.getPatches().size() + " patches:");
            for (PatchProposal patch : response.getPatches()) {
                System.out.println("    - " + patch.getFile());
                System.out.println("      " + patch.getDescription());
            }
        } else {
            System.out.println("  No patches generated");
        }
        
        System.out.println();
    }
    
    private String mapStateToTool(ExecutionState state) {
        switch (state) {
            case PLANNING:
                return "REPO_SEARCH";
            case CONTEXT_LOADING:
                return "SYMBOL_SEARCH";
            case EXECUTING:
                return "FILE_READ";
            case VERIFYING:
                return "PATCH_GENERATE";
            case PATCH_APPLYING:
                return "PATCH_APPLY";
            default:
                return null;
        }
    }
}
