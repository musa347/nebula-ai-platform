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
            You are a code modification assistant. Your task is to generate a MINIMAL PATCH for the provided code.
            
            TASK: %s
            FILE: %s
            
            CURRENT CODE:
            ```
            %s
            ```
            
            CONTEXT: %s
            
            CRITICAL INSTRUCTIONS:
            1. Generate ONLY the minimal code changes needed (patch/diff style)
            2. Include ONLY the lines that need to be added or modified
            3. DO NOT return the entire file - only the changed sections
            4. For adding comments: return just the comment lines to be inserted
            5. For modifying methods: return just the modified method
            6. Keep changes as small and targeted as possible
            
            EXAMPLE for "add comment at top":
            {"file":"Example.java","description":"Added class documentation","suggestedChange":"/**\n * This class handles example operations.\n */"}
            
            RESPOND WITH ONLY THIS JSON (no other text):
            {"file":"%s","description":"brief description of changes","suggestedChange":"MINIMAL PATCH CODE HERE"}
            
            JSON:
            """;

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
                context,
                file
        );
    }

    public String buildPatchPrompt(String task, String file, String filePreview, String context) {
        AiPatchRequest request = new AiPatchRequest(task, file, context, filePreview);
        return buildPatchPrompt(request);
    }

    private String validateAndDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public String getPromptTemplate() {
        return PATCH_PROMPT_TEMPLATE;
    }
}