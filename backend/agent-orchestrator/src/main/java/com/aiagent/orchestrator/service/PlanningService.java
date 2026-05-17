package com.aiagent.orchestrator.service;

import com.aiagent.common.enums.ToolType;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.PlanStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PlanningService {

    private static final Logger log = LoggerFactory.getLogger(PlanningService.class);

    // Patterns for task analysis
    private static final Pattern CLASS_SERVICE_PATTERN = Pattern.compile("\\b\\w*(Service|Controller|Repository|Manager|Handler)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUTATION_PATTERN = Pattern.compile("\\b(add|fix|update|create|modify|change|implement|refactor)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern READ_ONLY_PATTERN = Pattern.compile("\\b(analyze|review|check|inspect|examine|read)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CACHE_PATTERN = Pattern.compile("\\b(cache|caching)\\b", Pattern.CASE_INSENSITIVE);

    @Value("${ai.planning.enabled:false}")
    private boolean aiPlanningEnabled;

    private final AiEnhancedPlanningService aiEnhancedPlanningService;

    public PlanningService(AiEnhancedPlanningService aiEnhancedPlanningService) {
        this.aiEnhancedPlanningService = aiEnhancedPlanningService;
    }

    public ExecutionPlan createPlan(String task) {
        if (task == null || task.trim().isEmpty()) {
            return createMinimalPlan();
        }

        // Create deterministic plan first
        ExecutionPlan deterministicPlan = createDeterministicPlan(task);
        
        // If AI planning is enabled, try to enhance the plan
        if (aiPlanningEnabled) {
            try {
                ExecutionPlan enhancedPlan = aiEnhancedPlanningService.enhancePlan(task, deterministicPlan);
                if (enhancedPlan != null) {
                    log.info("Using AI-enhanced plan for task: {}", task);
                    return enhancedPlan;
                }
            } catch (Exception e) {
                log.warn("AI plan enhancement failed, using deterministic plan. Error: {}", e.getMessage());
            }
        }
        
        log.info("Using deterministic plan for task: {}", task);
        return deterministicPlan;
    }

    private ExecutionPlan createDeterministicPlan(String task) {
        String executionId = UUID.randomUUID().toString();
        List<PlanStep> steps = new ArrayList<>();
        int stepOrder = 1;

        log.debug("Creating execution plan for task: {}", task);

        // Rule 1 — Always start with SYMBOL_SEARCH if task contains class/service name
        if (containsClassOrService(task)) {
            String target = extractTarget(task);
            steps.add(new PlanStep(stepOrder++, "Locate target class", ToolType.SYMBOL_SEARCH, target));
        }

        // Rule 2 — Always load context next
        String contextTarget = extractContextTarget(task);
        steps.add(new PlanStep(stepOrder++, "Load related context", ToolType.CONTEXT_LOAD, contextTarget));

        // Rule 3 — Always read file before patch
        String fileTarget = extractFileTarget(task);
        steps.add(new PlanStep(stepOrder++, "Read implementation", ToolType.FILE_READ, fileTarget));

        // Rule 4 — If task contains mutation words, add patch steps
        if (isMutationTask(task)) {
            steps.add(new PlanStep(stepOrder++, "Generate patch", ToolType.PATCH_GENERATE, fileTarget));
            steps.add(new PlanStep(stepOrder++, "Apply patch safely", ToolType.PATCH_APPLY, fileTarget));
        }

        ExecutionPlan plan = new ExecutionPlan(executionId, steps);
        log.info("Created execution plan with {} steps for task: {}", steps.size(), task);
        return plan;
    }

    private ExecutionPlan createMinimalPlan() {
        String executionId = UUID.randomUUID().toString();
        List<PlanStep> steps = List.of(
            new PlanStep(1, "Load context", ToolType.CONTEXT_LOAD, "")
        );
        return new ExecutionPlan(executionId, steps);
    }

    private boolean containsClassOrService(String task) {
        return CLASS_SERVICE_PATTERN.matcher(task).find();
    }

    private boolean isMutationTask(String task) {
        return MUTATION_PATTERN.matcher(task).find() && !READ_ONLY_PATTERN.matcher(task).find();
    }

    private String extractTarget(String task) {
        // Extract class/service name from task
        var matcher = CLASS_SERVICE_PATTERN.matcher(task);
        if (matcher.find()) {
            return matcher.group();
        }
        
        // Fallback: extract first capitalized word
        String[] words = task.split("\\s+");
        for (String word : words) {
            if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                return word;
            }
        }
        
        return "target";
    }

    private String extractContextTarget(String task) {
        if (CACHE_PATTERN.matcher(task).find()) {
            return "cache";
        }
        
        // Extract key domain words
        String lowerTask = task.toLowerCase();
        if (lowerTask.contains("user")) return "user";
        if (lowerTask.contains("auth")) return "auth";
        if (lowerTask.contains("data")) return "data";
        
        return "context";
    }

    private String extractFileTarget(String task) {
        String target = extractTarget(task);
        if (target.endsWith("Service") || target.endsWith("Controller") || 
            target.endsWith("Repository") || target.endsWith("Manager")) {
            return target + ".java";
        }
        return target + ".java";
    }
}