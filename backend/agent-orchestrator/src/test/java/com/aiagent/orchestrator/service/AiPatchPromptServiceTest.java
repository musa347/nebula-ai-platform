package com.aiagent.orchestrator.service;

import com.aiagent.orchestrator.dto.AiPatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPatchPromptServiceTest {

    private AiPatchPromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new AiPatchPromptService();
    }

    @Test
    void testBuildPatchPromptWithValidRequest() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Add caching to UserService",
            "UserService.java",
            "Service layer with user operations",
            "public class UserService { public User findById(Long id) { return userRepository.findById(id); } }"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("You are a precise code modification assistant"));
        assertTrue(prompt.contains("Add caching to UserService"));
        assertTrue(prompt.contains("UserService.java"));
        assertTrue(prompt.contains("Service layer with user operations"));
        assertTrue(prompt.contains("public class UserService"));
        assertTrue(prompt.contains("Do NOT introduce new features outside scope"));
        assertTrue(prompt.contains("Return ONLY valid JSON"));
    }

    @Test
    void testBuildPatchPromptWithIndividualParameters() {
        // Given
        String task = "Fix null pointer exception";
        String file = "UserController.java";
        String filePreview = "public class UserController { public User getUser(Long id) { return service.findById(id); } }";
        String context = "Controller layer handling user requests";

        // When
        String prompt = promptService.buildPatchPrompt(task, file, filePreview, context);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Fix null pointer exception"));
        assertTrue(prompt.contains("UserController.java"));
        assertTrue(prompt.contains("public class UserController"));
        assertTrue(prompt.contains("Controller layer handling user requests"));
    }

    @Test
    void testBuildPatchPromptWithNullRequest() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> promptService.buildPatchPrompt((AiPatchRequest) null)
        );
        
        assertEquals("AiPatchRequest cannot be null", exception.getMessage());
    }

    @Test
    void testBuildPatchPromptWithNullFields() {
        // Given
        AiPatchRequest request = new AiPatchRequest(null, null, null, null);

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("No task specified"));
        assertTrue(prompt.contains("Unknown file"));
        assertTrue(prompt.contains("No file content available"));
        assertTrue(prompt.contains("No additional context"));
    }

    @Test
    void testBuildPatchPromptWithEmptyFields() {
        // Given
        AiPatchRequest request = new AiPatchRequest("", "   ", "", "   ");

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("No task specified"));
        assertTrue(prompt.contains("Unknown file"));
        assertTrue(prompt.contains("No file content available"));
        assertTrue(prompt.contains("No additional context"));
    }

    @Test
    void testBuildPatchPromptWithPartialData() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Add validation",
            "UserService.java",
            null,
            "public class UserService { }"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Add validation"));
        assertTrue(prompt.contains("UserService.java"));
        assertTrue(prompt.contains("No additional context"));
        assertTrue(prompt.contains("public class UserService"));
    }

    @Test
    void testPromptContainsAllRequiredRules() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Test task",
            "TestFile.java",
            "Test context",
            "Test content"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertTrue(prompt.contains("Do NOT introduce new features outside scope"));
        assertTrue(prompt.contains("Do NOT remove safety checks"));
        assertTrue(prompt.contains("Do NOT delete unrelated code"));
        assertTrue(prompt.contains("Return ONLY valid JSON"));
    }

    @Test
    void testPromptContainsJsonStructure() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Test task",
            "TestFile.java",
            "Test context",
            "Test content"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertTrue(prompt.contains("\"file\": \"...\""));
        assertTrue(prompt.contains("\"description\": \"...\""));
        assertTrue(prompt.contains("\"suggestedChange\": \"...\""));
    }

    @Test
    void testGetPromptTemplate() {
        // When
        String template = promptService.getPromptTemplate();

        // Then
        assertNotNull(template);
        assertTrue(template.contains("You are a precise code modification assistant"));
        assertTrue(template.contains("TASK:"));
        assertTrue(template.contains("FILE:"));
        assertTrue(template.contains("FILE CONTENT:"));
        assertTrue(template.contains("RELATED CONTEXT:"));
        assertTrue(template.contains("RULES:"));
    }

    @Test
    void testPromptStructureAndFormatting() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Implement error handling",
            "PaymentService.java",
            "Payment processing service with transaction handling",
            "public class PaymentService { public void processPayment(Payment payment) { /* implementation */ } }"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        // Verify sections are properly formatted
        assertTrue(prompt.contains("TASK:\nImplement error handling"));
        assertTrue(prompt.contains("FILE:\nPaymentService.java"));
        assertTrue(prompt.contains("FILE CONTENT:\npublic class PaymentService"));
        assertTrue(prompt.contains("RELATED CONTEXT:\nPayment processing service"));
    }

    @Test
    void testPromptWithSpecialCharacters() {
        // Given
        AiPatchRequest request = new AiPatchRequest(
            "Fix \"quotes\" and 'apostrophes'",
            "Test&File.java",
            "Context with <tags> and {braces}",
            "String message = \"Hello, world!\"; // Comment with 'quotes'"
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Fix \"quotes\" and 'apostrophes'"));
        assertTrue(prompt.contains("Test&File.java"));
        assertTrue(prompt.contains("Context with <tags> and {braces}"));
        assertTrue(prompt.contains("String message = \"Hello, world!\""));
    }

    @Test
    void testPromptWithLongContent() {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContent.append("public void method").append(i).append("() { /* implementation */ }\n");
        }
        
        AiPatchRequest request = new AiPatchRequest(
            "Refactor large class",
            "LargeService.java",
            "Large service with many methods",
            longContent.toString()
        );

        // When
        String prompt = promptService.buildPatchPrompt(request);

        // Then
        assertNotNull(prompt);
        assertTrue(prompt.contains("Refactor large class"));
        assertTrue(prompt.contains("LargeService.java"));
        assertTrue(prompt.contains("method0()"));
        assertTrue(prompt.contains("method99()"));
    }
}