package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiFailureRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiFailurePromptServiceTest {

    private AiFailurePromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new AiFailurePromptService();
    }

    @Test
    void testBuildFailurePromptWithValidRequest() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Add caching to UserService",
            "VERIFYING",
            "PATCH_GENERATE",
            "Error: compilation failed\nMissing import statement",
            "Compiling UserService.java...\nFound 1 error",
            "UserService.java context with CacheService reference"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("You are a software failure analysis assistant"));
        assertTrue(prompt.contains("Add caching to UserService"));
        assertTrue(prompt.contains("VERIFYING"));
        assertTrue(prompt.contains("PATCH_GENERATE"));
        assertTrue(prompt.contains("Error: compilation failed"));
        assertTrue(prompt.contains("Compiling UserService.java"));
        assertTrue(prompt.contains("UserService.java context"));
        assertTrue(prompt.contains("Analyze:"));
        assertTrue(prompt.contains("1. Root cause"));
        assertTrue(prompt.contains("2. Failure type"));
        assertTrue(prompt.contains("3. Suggested recovery"));
        assertTrue(prompt.contains("Return ONLY valid JSON"));
    }

    @Test
    void testBuildFailurePromptWithIndividualParameters() {
        // Given
        String task = "Fix bug in UserService";
        String state = "EXECUTING";
        String tool = "FILE_READ";
        String stderr = "FileNotFoundException: UserService.java not found";
        String stdout = "Reading file...";
        String context = "File path: /src/main/java/UserService.java";

        // When
        String prompt = promptService.buildFailurePrompt(task, state, tool, stderr, stdout, context);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Fix bug in UserService"));
        assertTrue(prompt.contains("EXECUTING"));
        assertTrue(prompt.contains("FILE_READ"));
        assertTrue(prompt.contains("FileNotFoundException"));
        assertTrue(prompt.contains("Reading file..."));
        assertTrue(prompt.contains("File path"));
    }

    @Test
    void testBuildFailurePromptWithNullRequest() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> promptService.buildFailurePrompt((AiFailureRequest) null)
        );
        
        assertEquals("AiFailureRequest cannot be null", exception.getMessage());
    }

    @Test
    void testBuildFailurePromptWithNullFields() {
        // Given
        AiFailureRequest request = new AiFailureRequest(null, null, null, null, null, null);

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("No task specified"));
        assertTrue(prompt.contains("Unknown state"));
        assertTrue(prompt.contains("Unknown tool"));
        assertTrue(prompt.contains("No error output"));
        assertTrue(prompt.contains("No standard output"));
        assertTrue(prompt.contains("No additional context"));
    }

    @Test
    void testBuildFailurePromptWithEmptyFields() {
        // Given
        AiFailureRequest request = new AiFailureRequest("", "   ", "", "   ", "", "   ");

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("No task specified"));
        assertTrue(prompt.contains("Unknown state"));
        assertTrue(prompt.contains("Unknown tool"));
        assertTrue(prompt.contains("No error output"));
        assertTrue(prompt.contains("No standard output"));
        assertTrue(prompt.contains("No additional context"));
    }

    @Test
    void testBuildFailurePromptWithPartialData() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Add validation",
            "PATCH_APPLYING",
            null,
            "Patch failed",
            null,
            "Validation context"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Add validation"));
        assertTrue(prompt.contains("PATCH_APPLYING"));
        assertTrue(prompt.contains("Unknown tool"));
        assertTrue(prompt.contains("Patch failed"));
        assertTrue(prompt.contains("No standard output"));
        assertTrue(prompt.contains("Validation context"));
    }

    @Test
    void testPromptContainsAllRequiredSections() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Test task",
            "FAILED",
            "TEST_TOOL",
            "Test error",
            "Test output",
            "Test context"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertTrue(prompt.contains("TASK:"));
        assertTrue(prompt.contains("EXECUTION STATE:"));
        assertTrue(prompt.contains("TOOL:"));
        assertTrue(prompt.contains("STDERR:"));
        assertTrue(prompt.contains("STDOUT:"));
        assertTrue(prompt.contains("CONTEXT:"));
        assertTrue(prompt.contains("Analyze:"));
    }

    @Test
    void testPromptContainsJsonStructure() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Test task",
            "FAILED",
            "TEST_TOOL",
            "Test error",
            "Test output",
            "Test context"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertTrue(prompt.contains("\"failureType\": \"...\""));
        assertTrue(prompt.contains("\"rootCause\": \"...\""));
        assertTrue(prompt.contains("\"suggestedRecovery\": \"...\""));
    }

    @Test
    void testGetPromptTemplate() {
        // When
        String template = promptService.getPromptTemplate();

        // Then
        assertNotNull(template);
        assertTrue(template.contains("You are a software failure analysis assistant"));
        assertTrue(template.contains("TASK:"));
        assertTrue(template.contains("EXECUTION STATE:"));
        assertTrue(template.contains("TOOL:"));
        assertTrue(template.contains("STDERR:"));
        assertTrue(template.contains("STDOUT:"));
        assertTrue(template.contains("CONTEXT:"));
        assertTrue(template.contains("Analyze:"));
    }

    @Test
    void testPromptStructureAndFormatting() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Implement error handling",
            "VERIFYING",
            "PATCH_GENERATE",
            "NullPointerException at line 42",
            "Generating patch for UserService",
            "Error handling context with try-catch blocks"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        // Verify sections are properly formatted
        assertTrue(prompt.contains("TASK:\nImplement error handling"));
        assertTrue(prompt.contains("EXECUTION STATE:\nVERIFYING"));
        assertTrue(prompt.contains("TOOL:\nPATCH_GENERATE"));
        assertTrue(prompt.contains("STDERR:\nNullPointerException at line 42"));
        assertTrue(prompt.contains("STDOUT:\nGenerating patch for UserService"));
        assertTrue(prompt.contains("CONTEXT:\nError handling context"));
    }

    @Test
    void testPromptWithSpecialCharacters() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Fix \"quotes\" and 'apostrophes'",
            "FAILED",
            "PATCH_APPLY",
            "Error: unexpected character '\"' at position 42",
            "Output with <tags> and {braces}",
            "Context with special chars: @#$%^&*()"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Fix \"quotes\" and 'apostrophes'"));
        assertTrue(prompt.contains("Error: unexpected character '\"'"));
        assertTrue(prompt.contains("Output with <tags> and {braces}"));
        assertTrue(prompt.contains("Context with special chars: @#$%^&*()"));
    }

    @Test
    void testPromptWithMultilineContent() {
        // Given
        String multilineStderr = "Error occurred:\n" +
                                "Line 1: Syntax error\n" +
                                "Line 2: Missing semicolon\n" +
                                "Line 3: Unexpected token";
        
        String multilineStdout = "Compilation started\n" +
                                "Processing file 1\n" +
                                "Processing file 2\n" +
                                "Compilation failed";
        
        AiFailureRequest request = new AiFailureRequest(
            "Compile project",
            "EXECUTING",
            "SHELL_EXECUTE",
            multilineStderr,
            multilineStdout,
            "Build context"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Line 1: Syntax error"));
        assertTrue(prompt.contains("Line 2: Missing semicolon"));
        assertTrue(prompt.contains("Processing file 1"));
        assertTrue(prompt.contains("Compilation failed"));
    }

    @Test
    void testPromptWithLongContent() {
        // Given
        StringBuilder longStderr = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longStderr.append("Error line ").append(i).append("\n");
        }
        
        AiFailureRequest request = new AiFailureRequest(
            "Complex task",
            "FAILED",
            "COMPLEX_TOOL",
            longStderr.toString(),
            "Long output",
            "Long context"
        );

        // When
        String prompt = promptService.buildFailurePrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Complex task"));
        assertTrue(prompt.contains("Error line 0"));
        assertTrue(prompt.contains("Error line 99"));
    }

    @Test
    void testDeterministicFormatting() {
        // Given
        AiFailureRequest request = new AiFailureRequest(
            "Test task",
            "FAILED",
            "TEST_TOOL",
            "Test error",
            "Test output",
            "Test context"
        );

        // When
        String prompt1 = promptService.buildFailurePrompt(request);
        String prompt2 = promptService.buildFailurePrompt(request);

        // Then
        assertEquals(prompt1, prompt2, "Prompt generation should be deterministic");
    }
}