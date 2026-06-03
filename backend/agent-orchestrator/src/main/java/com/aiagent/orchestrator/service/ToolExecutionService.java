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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);

    @Value("${ai.patch.enabled:false}")
    private boolean aiPatchEnabled;

    @Autowired
    private ContextLoaderService contextLoaderService;

    @Autowired
    private FileReaderService fileReaderService;

    @Autowired
    private PatchProposalService patchProposalService;

    @Autowired
    private AiPatchGenerationService aiPatchGenerationService;

    @Autowired
    private PatchExecutionService patchExecutionService;

    @Autowired
    private ExecutionStatsService executionStatsService;

    public static class ToolExecutionResult {
        private boolean success;
        private String message;
        private Object data;

        public ToolExecutionResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }

    public ToolExecutionResult execute(ToolDecision decision, String task, String targetFile) {
        return execute(decision, task, targetFile, null, null);
    }

    public ToolExecutionResult execute(ToolDecision decision, String task, String targetFile, List<String> workspaceFiles) {
        return execute(decision, task, targetFile, workspaceFiles, null);
    }

    public ToolExecutionResult execute(ToolDecision decision, String task, String targetFile, List<String> workspaceFiles, List<PatchProposal> existingPatches) {
        return execute(decision, task, targetFile, workspaceFiles, existingPatches, null);
    }

    public ToolExecutionResult execute(ToolDecision decision, String task, String targetFile, List<String> workspaceFiles, List<PatchProposal> existingPatches, String workspacePath) {
        if (decision == null || decision.getToolType() == null) {
            return new ToolExecutionResult(false, "Invalid tool decision", null);
        }

        long startTime = System.currentTimeMillis();
        ToolExecutionResult result;

        try {
            result = executeByType(decision.getToolType(), task, targetFile, workspaceFiles, existingPatches, workspacePath);
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", decision.getToolType(), e.getMessage(), e);
            result = new ToolExecutionResult(false, "Tool execution error: " + e.getMessage(), null);
        }

        long duration = System.currentTimeMillis() - startTime;
        if (result.isSuccess()) {
            executionStatsService.recordSuccess(decision.getToolType(), duration);
        } else {
            executionStatsService.recordFailure(decision.getToolType(), duration);
        }

        return result;
    }

    private ToolExecutionResult executeByType(ToolType toolType, String task, String targetFile, List<String> workspaceFiles, List<PatchProposal> existingPatches, String workspacePath) {
        switch (toolType) {
            case REPO_SEARCH:
                return executeRepoSearch(task, targetFile, workspaceFiles);

            case SYMBOL_SEARCH:
            case DEPENDENCY_ANALYSIS:
                return executeContextLoad(task, targetFile, workspaceFiles);

            case FILE_READ:
                return executeFileRead(task, targetFile, workspaceFiles, workspacePath);

            case PATCH_GENERATE:
                return executePatchGenerate(task, targetFile, workspaceFiles, workspacePath);

            case PATCH_APPLY:
                return executePatchApply(task, targetFile, workspaceFiles, existingPatches, workspacePath);

            case NONE:
                return new ToolExecutionResult(true, "No tool execution needed", null);

            default:
                return new ToolExecutionResult(false, "Unsupported tool type: " + toolType, null);
        }
    }

    private ToolExecutionResult executeRepoSearch(String task, String targetFile, List<String> workspaceFiles) {
        List<LoadedContext> contexts = contextLoaderService.loadContext(task, targetFile, workspaceFiles);

        log.info("REPO_SEARCH executed: found {} contexts", contexts.size());
        return new ToolExecutionResult(true, "Repository search completed", contexts);
    }

    private ToolExecutionResult executeContextLoad(String task, String targetFile, List<String> workspaceFiles) {
        List<LoadedContext> contexts = contextLoaderService.loadContext(task, targetFile, workspaceFiles);

        log.info("CONTEXT_LOAD executed: loaded {} contexts", contexts.size());
        return new ToolExecutionResult(true, "Context loading completed", contexts);
    }

    private ToolExecutionResult executeFileRead(String task, String targetFile, List<String> workspaceFiles, String workspacePath) {
        List<LoadedContext> contexts = contextLoaderService.loadContext(task, targetFile, workspaceFiles);

        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts, workspacePath);

        log.info("FILE_READ executed: read {} file previews", previews.size());
        return new ToolExecutionResult(true, "File reading completed", previews);
    }

    private ToolExecutionResult executePatchGenerate(String task, String targetFile, List<String> workspaceFiles, String workspacePath) {
        List<LoadedContext> contexts = contextLoaderService.loadContext(task, targetFile, workspaceFiles);

        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts, workspacePath);
        List<PatchProposal> patches;

        if (aiPatchEnabled) {
            try {
                log.info("Using AI patch generation for task: {}", task);
                patches = generatePatchesWithAi(task, contexts, previews);

                if (patches != null && !patches.isEmpty()) {
                    log.info("AI PATCH_GENERATE executed: generated {} patches", patches.size());
                    return new ToolExecutionResult(true, "AI patch generation completed", patches);
                }

                log.warn("AI patch generation returned empty results, falling back to rule-based engine");
            } catch (Exception e) {
                log.warn("AI patch generation failed, falling back to rule-based engine. Error: {}", e.getMessage());
            }
        }

        log.info("Using rule-based patch generation for task: {}", task);
        patches = patchProposalService.generatePatches(task, contexts, previews);

        log.info("RULE-BASED PATCH_GENERATE executed: generated {} patches", patches.size());
        return new ToolExecutionResult(true, "Rule-based patch generation completed", patches);
    }

    private List<PatchProposal> generatePatchesWithAi(String task, List<LoadedContext> contexts, List<FilePreview> previews) {
        if (previews == null || previews.isEmpty()) {
            log.warn("No file previews available for AI patch generation");
            return null;
        }

        String contextInfo = buildContextInfo(contexts);

        Set<String> knownFiles = contexts.stream()
                .map(LoadedContext::getFile)
                .collect(Collectors.toSet());

        List<PatchProposal> allPatches = new java.util.ArrayList<>();

        for (FilePreview preview : previews) {
            try {
                List<PatchProposal> filePatches = aiPatchGenerationService.generatePatches(
                        task,
                        contextInfo,
                        preview.getPreview(),
                        knownFiles
                );

                if (filePatches != null) {
                    allPatches.addAll(filePatches);
                }
            } catch (Exception e) {
                log.warn("Failed to generate AI patches for file {}: {}", preview.getFile(), e.getMessage());
            }
        }

        return allPatches;
    }

    private String buildContextInfo(List<LoadedContext> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return "No additional context available";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Available context files:\n");

        for (LoadedContext context : contexts) {
            contextBuilder.append("- ").append(context.getFile())
                    .append(" (").append(context.getReason()).append(")\n");
        }

        return contextBuilder.toString();
    }

    private ToolExecutionResult executePatchApply(String task, String targetFile, List<String> workspaceFiles, List<PatchProposal> existingPatches, String workspacePath) {
        List<LoadedContext> contexts = contextLoaderService.loadContext(task, targetFile, workspaceFiles);

        List<FilePreview> previews = fileReaderService.readFilePreviews(contexts, workspacePath);
        
        List<PatchProposal> patches;
        if (existingPatches != null && !existingPatches.isEmpty()) {
            log.info("Using {} pre-generated patches", existingPatches.size());
            patches = existingPatches;
        } else {
            patches = patchProposalService.generatePatches(task, contexts, previews);
        }

        List<PatchExecutionResult> results = patchExecutionService.executePatches(patches, workspacePath);

        log.info("PATCH_APPLY executed: applied {} patches", results.size());
        return new ToolExecutionResult(true, "Patch application completed", results);
    }
}