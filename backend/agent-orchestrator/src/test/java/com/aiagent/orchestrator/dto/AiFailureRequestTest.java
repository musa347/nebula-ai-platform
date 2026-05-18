package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiFailureRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDefaultConstructor() {
        AiFailureRequest request = new AiFailureRequest();
        
        assertNull(request.getTask());
        assertNull(request.getExecutionState());
        assertNull(request.getToolName());
        assertNull(request.getStderr());
        assertNull(request.getStdout());
        assertNull(request.getContext());
    }

    @Test
    void testParameterizedConstructor() {
        String task = "Add caching to UserService";
        String state = "VERIFYING";
        String tool = "PATCH_GENERATE";
        String stderr = "Error: compilation failed";
        String stdout = "Compiling...";
        String context = "UserService.java context";
        
        AiFailureRequest request = new AiFailureRequest(task, state, tool, stderr, stdout, context);
        
        assertEquals(task, request.getTask());
        assertEquals(state, request.getExecutionState());
        assertEquals(tool, request.getToolName());
        assertEquals(stderr, request.getStderr());
        assertEquals(stdout, request.getStdout());
        assertEquals(context, request.getContext());
    }

    @Test
    void testSettersAndGetters() {
        AiFailureRequest request = new AiFailureRequest();
        
        request.setTask("Fix bug in UserService");
        request.setExecutionState("EXECUTING");
        request.setToolName("FILE_READ");
        request.setStderr("FileNotFoundException");
        request.setStdout("Reading file...");
        request.setContext("File context");
        
        assertEquals("Fix bug in UserService", request.getTask());
        assertEquals("EXECUTING", request.getExecutionState());
        assertEquals("FILE_READ", request.getToolName());
        assertEquals("FileNotFoundException", request.getStderr());
        assertEquals("Reading file...", request.getStdout());
        assertEquals("File context", request.getContext());
    }

    @Test
    void testJsonSerialization() throws Exception {
        AiFailureRequest request = new AiFailureRequest(
            "Add logging",
            "PATCH_APPLYING",
            "PATCH_APPLY",
            "Error: patch failed",
            "Applying patch...",
            "Patch context"
        );
        
        String json = objectMapper.writeValueAsString(request);
        
        assertTrue(json.contains("\"task\":\"Add logging\""));
        assertTrue(json.contains("\"executionState\":\"PATCH_APPLYING\""));
        assertTrue(json.contains("\"toolName\":\"PATCH_APPLY\""));
        assertTrue(json.contains("\"stderr\":\"Error: patch failed\""));
        assertTrue(json.contains("\"stdout\":\"Applying patch...\""));
        assertTrue(json.contains("\"context\":\"Patch context\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "task": "Update UserService",
                "executionState": "VERIFYING",
                "toolName": "PATCH_GENERATE",
                "stderr": "Syntax error",
                "stdout": "Generating patch",
                "context": "Service context"
            }
            """;
        
        AiFailureRequest request = objectMapper.readValue(json, AiFailureRequest.class);
        
        assertEquals("Update UserService", request.getTask());
        assertEquals("VERIFYING", request.getExecutionState());
        assertEquals("PATCH_GENERATE", request.getToolName());
        assertEquals("Syntax error", request.getStderr());
        assertEquals("Generating patch", request.getStdout());
        assertEquals("Service context", request.getContext());
    }

    @Test
    void testJsonSerializationWithNullValues() throws Exception {
        AiFailureRequest request = new AiFailureRequest();
        
        String json = objectMapper.writeValueAsString(request);
        
        assertTrue(json.contains("\"task\":null"));
        assertTrue(json.contains("\"executionState\":null"));
        assertTrue(json.contains("\"toolName\":null"));
        assertTrue(json.contains("\"stderr\":null"));
        assertTrue(json.contains("\"stdout\":null"));
        assertTrue(json.contains("\"context\":null"));
    }

    @Test
    void testJsonDeserializationWithMissingFields() throws Exception {
        String json = """
            {
                "task": "Test task",
                "executionState": "FAILED"
            }
            """;
        
        AiFailureRequest request = objectMapper.readValue(json, AiFailureRequest.class);
        
        assertEquals("Test task", request.getTask());
        assertEquals("FAILED", request.getExecutionState());
        assertNull(request.getToolName());
        assertNull(request.getStderr());
        assertNull(request.getStdout());
        assertNull(request.getContext());
    }

    @Test
    void testToString() {
        AiFailureRequest request = new AiFailureRequest(
            "Test task",
            "EXECUTING",
            "FILE_READ",
            "Error message",
            "Output message",
            "Context info"
        );
        
        String toString = request.toString();
        
        assertTrue(toString.contains("task='Test task'"));
        assertTrue(toString.contains("executionState='EXECUTING'"));
        assertTrue(toString.contains("toolName='FILE_READ'"));
        assertTrue(toString.contains("chars")); // stderr, stdout, context lengths
    }

    @Test
    void testToStringWithNullValues() {
        AiFailureRequest request = new AiFailureRequest(
            "Test task",
            "FAILED",
            "PATCH_APPLY",
            null,
            null,
            null
        );
        
        String toString = request.toString();
        
        assertTrue(toString.contains("stderr='null'"));
        assertTrue(toString.contains("stdout='null'"));
        assertTrue(toString.contains("context='null'"));
    }

    @Test
    void testWithLongErrorMessages() throws Exception {
        StringBuilder longStderr = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longStderr.append("Error line ").append(i).append("\n");
        }
        
        AiFailureRequest request = new AiFailureRequest(
            "Complex task",
            "FAILED",
            "SHELL_EXECUTE",
            longStderr.toString(),
            "Command output",
            "Execution context"
        );
        
        // Test serialization with long content
        String json = objectMapper.writeValueAsString(request);
        AiFailureRequest deserialized = objectMapper.readValue(json, AiFailureRequest.class);
        
        assertEquals(request.getTask(), deserialized.getTask());
        assertEquals(request.getStderr(), deserialized.getStderr());
        assertTrue(deserialized.getStderr().length() > 10000);
    }

    @Test
    void testWithSpecialCharacters() throws Exception {
        AiFailureRequest request = new AiFailureRequest(
            "Fix \"quotes\" and 'apostrophes'",
            "FAILED",
            "PATCH_APPLY",
            "Error: <tag> & {brace}",
            "Output: line1\nline2\ttab",
            "Context with special chars: @#$%"
        );
        
        String json = objectMapper.writeValueAsString(request);
        AiFailureRequest deserialized = objectMapper.readValue(json, AiFailureRequest.class);
        
        assertEquals(request.getTask(), deserialized.getTask());
        assertEquals(request.getStderr(), deserialized.getStderr());
        assertEquals(request.getStdout(), deserialized.getStdout());
    }
}