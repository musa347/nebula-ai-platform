package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiFailureRequest;
import org.springframework.stereotype.Service;

@Service
public class AiFailurePromptService {

    private static final String FAILURE_PROMPT_TEMPLATE = """
            You are a software failure analysis assistant.
            
            TASK:
            %s
            
            EXECUTION STATE:
            %s
            
            TOOL:
            %s
            
            STDERR:
            %s
            
            STDOUT:
            %s
            
            CONTEXT:
            %s
            
            Analyze:
            1. Root cause
            2. Failure type
            3. Suggested recovery
            
            Return ONLY valid JSON:
            {
              "failureType": "...",
              "rootCause": "...",
              "suggestedRecovery": "..."
            }
            """;

    public String buildFailurePrompt(AiFailureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFailureRequest cannot be null");
        }

        String task = validateAndDefault(request.getTask(), "No task specified");
        String state = validateAndDefault(request.getExecutionState(), "Unknown state");
        String tool = validateAndDefault(request.getToolName(), "Unknown tool");
        String stderr = validateAndDefault(request.getStderr(), "No error output");
        String stdout = validateAndDefault(request.getStdout(), "No standard output");
        String context = validateAndDefault(request.getContext(), "No additional context");

        return String.format(
            FAILURE_PROMPT_TEMPLATE,
            task,
            state,
            tool,
            stderr,
            stdout,
            context
        );
    }

    public String buildFailurePrompt(String task, String executionState, String toolName,
                                    String stderr, String stdout, String context) {
        AiFailureRequest request = new AiFailureRequest(task, executionState, toolName, stderr, stdout, context);
        return buildFailurePrompt(request);
    }

    private String validateAndDefault(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    public String getPromptTemplate() {
        return FAILURE_PROMPT_TEMPLATE;
    }
}