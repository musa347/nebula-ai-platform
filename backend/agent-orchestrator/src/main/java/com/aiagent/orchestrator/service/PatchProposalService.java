package com.aiagent.orchestrator.service;

import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.PatchProposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatchProposalService {

    private static final Logger log = LoggerFactory.getLogger(PatchProposalService.class);

    public List<PatchProposal> generatePatches(String task, List<LoadedContext> contexts, List<FilePreview> previews) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (task == null || task.trim().isEmpty()) {
            return patches;
        }
        
        if (contexts == null || contexts.isEmpty()) {
            return patches;
        }

        String lowerTask = task.toLowerCase();
        
        // Rule-based patch generation using deterministic heuristics
        patches.addAll(generateCachingPatches(lowerTask, contexts));
        patches.addAll(generateOptimizationPatches(lowerTask, contexts));
        patches.addAll(generateValidationPatches(lowerTask, contexts));
        patches.addAll(generateLoggingPatches(lowerTask, contexts));
        patches.addAll(generateSecurityPatches(lowerTask, contexts));
        
        log.info("Generated {} patch proposals for task: {}", patches.size(), task);
        return patches;
    }

    private List<PatchProposal> generateCachingPatches(String lowerTask, List<LoadedContext> contexts) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (lowerTask.contains("cache") || lowerTask.contains("caching")) {
            for (LoadedContext context : contexts) {
                if (context.getFile().contains("Service.java")) {
                    patches.add(new PatchProposal(
                        context.getFile(),
                        "Add caching layer to reduce database calls",
                        "Introduce @Cacheable annotation and CacheService dependency"
                    ));
                }
            }
        }
        
        return patches;
    }

    private List<PatchProposal> generateOptimizationPatches(String lowerTask, List<LoadedContext> contexts) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (lowerTask.contains("optimize") || lowerTask.contains("performance")) {
            for (LoadedContext context : contexts) {
                if (context.getFile().contains("Service.java")) {
                    patches.add(new PatchProposal(
                        context.getFile(),
                        "Optimize method performance and reduce complexity",
                        "Refactor large methods into smaller, focused methods with better algorithms"
                    ));
                }
            }
        }
        
        return patches;
    }

    private List<PatchProposal> generateValidationPatches(String lowerTask, List<LoadedContext> contexts) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (lowerTask.contains("validation") || lowerTask.contains("validate")) {
            for (LoadedContext context : contexts) {
                if (context.getFile().contains("Service.java") || context.getFile().contains("Controller.java")) {
                    patches.add(new PatchProposal(
                        context.getFile(),
                        "Add input validation to prevent invalid data processing",
                        "Add @Valid annotations and null checks for method parameters"
                    ));
                }
            }
        }
        
        return patches;
    }

    private List<PatchProposal> generateLoggingPatches(String lowerTask, List<LoadedContext> contexts) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (lowerTask.contains("logging") || lowerTask.contains("log")) {
            for (LoadedContext context : contexts) {
                if (context.getFile().endsWith(".java")) {
                    patches.add(new PatchProposal(
                        context.getFile(),
                        "Add comprehensive logging for better observability",
                        "Insert logger statements at method entry/exit and error conditions"
                    ));
                }
            }
        }
        
        return patches;
    }

    private List<PatchProposal> generateSecurityPatches(String lowerTask, List<LoadedContext> contexts) {
        List<PatchProposal> patches = new ArrayList<>();
        
        if (lowerTask.contains("security") || lowerTask.contains("secure")) {
            for (LoadedContext context : contexts) {
                if (context.getFile().contains("Controller.java")) {
                    patches.add(new PatchProposal(
                        context.getFile(),
                        "Add security annotations and input sanitization",
                        "Add @PreAuthorize annotations and input validation filters"
                    ));
                }
            }
        }
        
        return patches;
    }
}