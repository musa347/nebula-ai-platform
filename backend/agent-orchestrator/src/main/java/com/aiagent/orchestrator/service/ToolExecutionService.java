package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ToolDecision;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
import com.aiagent.common.model.PatchExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;

@Service
public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);

    @Autowired
    private ContextLoaderService contextLoaderService;

    @Autowired
    private FileReaderService fileReaderService;

    @Autowired
    private PatchProposalService patchProposalService;

    @Autowired
    private PatchExecutionService patchExecutionService;

    public static class ToolExecutionResult {
        private boolean success;
        private String message;
        private Object data;

        public ToolExecutionResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    public ToolExecutionResult execute(ToolDecision decision, String task, String targetFile) {
        if (decision == null || decision.getToolType() == null) {
            return new ToolExecutionResult(false, "Invalid tool decision", null);
        }

        try {
            return executeByType(decision.getToolType(), task, targetFile);
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", decision.getToolType(), e.getMessage(), e);
            return new ToolExecutionResult(false, "Tool execution error: " + e.getMessage(), null);
        }
    }

    private ToolExecutionResult executeByType(ToolType toolType, String task, String targetFile) {
        switch (toolType) {
            case REPO_SEARCH:
                return executeRepoSearch(task, targetFile);

            case SYMBOL_SEARCH:
            case DEPENDENCY_ANALYSIS:
                return executeContextLoad(task, targetFile);

            case FILE_READ:
                return executeFileRead(task, targetFile);

            case PATCH_GENERATE:
                return executePatchGenerate(task, targetFile);

            case PATCH_APPLY:
                return executePatchApply(task, targetFile);

            case NONE:
                return new ToolExecutionResult(true, "No tool execution needed", null);

            default:
                return new ToolExecutionResult(false, "Unsupported tool type: " + toolType, null);
        }
    }

    private ToolExecutionResult executeRepoSearch(String task, String targetFile) {
        // Simulate repository search - in real implementation would call MCP server
        List<LoadedContext> contexts;
        if (targetFile != null) {
            contexts = contextLoaderService.loadContext(task, targetFile);
        } else {
            contexts = contextLoaderService.loadContext(task);
        }
        
        log.info("REPO_SEARCH executed: found {} contexts", contexts.size());
        return new ToolExecutionResult(true, "Repository search completed", contexts);
    }

    private ToolExecutionResult executeContextLoad(String task, String targetFile) {
        // Load additional context based on symbols or dependencies
        List<LoadedContext> contexts;
        if (targetFile != null) {
            contexts = contextLoaderService.loadContext(task, targetFile);
        } else {
            contexts = contextLoaderService.loadContext(task);
        }
        
        log.info("CONTEXT_LOAD executed: loaded {} contexts", contexts.size());
        return new ToolExecutionResult(true, "Context loading completed", contexts);
    }

    private ToolExecutionResult executeFileRead(String task, String targetFile) {
        // Read file previews for analysis
        List<LoadedContext> contexts = (List<LoadedContext>) 
            (targetFile != null ? contextLoaderService.loadContext(task, targetFile) 
                               : contextLoaderService.loadContext(task));
        
        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts);
        
        log.info("FILE_READ executed: read {} file previews", previews.size());
        return new ToolExecutionResult(true, "File reading completed", previews);
    }

    private ToolExecutionResult executePatchGenerate(String task, String targetFile) {
        // Generate patches based on current context
        List<LoadedContext> contexts = (List<LoadedContext>) 
            (targetFile != null ? contextLoaderService.loadContext(task, targetFile) 
                               : contextLoaderService.loadContext(task));
        
        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts);
        List<PatchProposal> patches = patchProposalService.generatePatches(task, contexts, previews);
        
        log.info("PATCH_GENERATE executed: generated {} patches", patches.size());
        return new ToolExecutionResult(true, "Patch generation completed", patches);
    }

    private ToolExecutionResult executePatchApply(String task, String targetFile) {
        // Apply patches - get patches from previous execution context
        // For now, simulate patch execution since we don't have state persistence between steps
        List<LoadedContext> contexts = (List<LoadedContext>) 
            (targetFile != null ? contextLoaderService.loadContext(task, targetFile) 
                               : contextLoaderService.loadContext(task));
        
        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts);
        List<PatchProposal> patches = patchProposalService.generatePatches(task, contexts, previews);
        List<PatchExecutionResult> results = patchExecutionService.executePatches(patches);
        
        log.info("PATCH_APPLY executed: applied {} patches", results.size());
        return new ToolExecutionResult(true, "Patch application completed", results);
    }
}