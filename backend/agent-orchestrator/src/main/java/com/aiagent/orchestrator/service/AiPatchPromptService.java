package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiPatchRequest;
import org.springframework.stereotype.Service;

/**
 * Service for building structured prompts for AI patch generation.
 * Provides consistent prompt formatting for code modification tasks.
 */
@Service
public class AiPatchPromptService {

    private static final String PATCH_PROMPT_TEMPLATE = """
            You are a precise code modification assistant.
            
            TASK:
            %s
            
            FILE:
            %s
            
            FILE CONTENT:
            %s
            
            RELATED CONTEXT:
            %s
            
            RULES:
            - Do NOT introduce new features outside scope
            - Do NOT remove safety checks
            - Do NOT delete unrelated code
            - Return ONLY valid JSON:
            {
              "file": "...",
              "description": "...",
              "suggestedChange": "..."
            }
            """;

    /**
     * Builds a structured prompt for AI patch generation.
     *
     * @param request The patch request containing task, file, context, and preview
     * @return Formatted prompt string ready for AI processing
     * @throws IllegalArgumentException if request or required fields are null/empty
     */
    public String buildPatchPrompt(AiPatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiPatchRequest cannot be null");
        }

        String task = validateAndDefault(request.getTask(), "No task specified");
        String file = validateAndDefault(request.getFile(), "Unknown file");
        String filePreview = validateAndDefault(request.getFilePreview(), "No file content available");
        String context = validateAndDefault(request.getContext(), "No additional context");

        return String.format(
            PATCH_PROMPT_TEMPLATE,
            task,
            file,
            filePreview,
            context
        );
    }

    /**
     * Builds a structured prompt with individual parameters.
     *
     * @param task The task description
     * @param file The target file name
     * @param filePreview The current file content
     * @param context Additional context information
     * @return Formatted prompt string ready for AI processing
     */
    public String buildPatchPrompt(String task, String file, String filePreview, String context) {
        AiPatchRequest request = new AiPatchRequest(task, file, context, filePreview);
        return buildPatchPrompt(request);
    }

    /**
     * Validates input and provides default value if null or empty.
     *
     * @param value The value to validate
     * @param defaultValue The default value to use if validation fails
     * @return The original value if valid, otherwise the default value
     */
    private String validateAndDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets the raw prompt template for testing or customization.
     *
     * @return The prompt template string
     */
    public String getPromptTemplate() {
        return PATCH_PROMPT_TEMPLATE;
    }
}