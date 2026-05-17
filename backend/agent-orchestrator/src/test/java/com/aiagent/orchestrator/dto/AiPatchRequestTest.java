package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPatchRequestTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDefaultConstructor() {
        AiPatchRequest request = new AiPatchRequest();
        
        assertNull(request.getTask());
        assertNull(request.getFile());
        assertNull(request.getContext());
        assertNull(request.getFilePreview());
    }

    @Test
    void testParameterizedConstructor() {
        String task = "Add caching to UserService";
        String file = "UserService.java";
        String context = "Service layer context";
        String filePreview = "public class UserService { ... }";
        
        AiPatchRequest request = new AiPatchRequest(task, file, context, filePreview);
        
        assertEquals(task, request.getTask());
        assertEquals(file, request.getFile());
        assertEquals(context, request.getContext());
        assertEquals(filePreview, request.getFilePreview());
    }

    @Test
    void testSettersAndGetters() {
        AiPatchRequest request = new AiPatchRequest();
        
        request.setTask("Fix bug in UserService");
        request.setFile("UserService.java");
        request.setContext("Bug fix context");
        request.setFilePreview("class UserService { buggyMethod() }");
        
        assertEquals("Fix bug in UserService", request.getTask());
        assertEquals("UserService.java", request.getFile());
        assertEquals("Bug fix context", request.getContext());
        assertEquals("class UserService { buggyMethod() }", request.getFilePreview());
    }

    @Test
    void testJsonSerialization() throws Exception {
        AiPatchRequest request = new AiPatchRequest(
            "Add logging to UserService",
            "UserService.java",
            "Logging enhancement context",
            "public class UserService { public void process() {} }"
        );
        
        String json = objectMapper.writeValueAsString(request);
        
        assertTrue(json.contains("\"task\":\"Add logging to UserService\""));
        assertTrue(json.contains("\"file\":\"UserService.java\""));
        assertTrue(json.contains("\"context\":\"Logging enhancement context\""));
        assertTrue(json.contains("\"filePreview\":\"public class UserService { public void process() {} }\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "task": "Update UserService method",
                "file": "UserService.java",
                "context": "Method update context",
                "filePreview": "public class UserService { oldMethod() }"
            }
            """;
        
        AiPatchRequest request = objectMapper.readValue(json, AiPatchRequest.class);
        
        assertEquals("Update UserService method", request.getTask());
        assertEquals("UserService.java", request.getFile());
        assertEquals("Method update context", request.getContext());
        assertEquals("public class UserService { oldMethod() }", request.getFilePreview());
    }

    @Test
    void testJsonSerializationWithNullValues() throws Exception {
        AiPatchRequest request = new AiPatchRequest();
        
        String json = objectMapper.writeValueAsString(request);
        
        assertTrue(json.contains("\"task\":null"));
        assertTrue(json.contains("\"file\":null"));
        assertTrue(json.contains("\"context\":null"));
        assertTrue(json.contains("\"filePreview\":null"));
    }

    @Test
    void testToString() {
        AiPatchRequest request = new AiPatchRequest(
            "Test task",
            "TestFile.java",
            "Test context",
            "public class TestFile { }"
        );
        
        String toString = request.toString();
        
        assertTrue(toString.contains("task='Test task'"));
        assertTrue(toString.contains("file='TestFile.java'"));
        assertTrue(toString.contains("context='Test context'"));
        assertTrue(toString.contains("chars")); // filePreview length
    }

    @Test
    void testToStringWithNullFilePreview() {
        AiPatchRequest request = new AiPatchRequest(
            "Test task",
            "TestFile.java",
            "Test context",
            null
        );
        
        String toString = request.toString();
        
        assertTrue(toString.contains("filePreview='null'"));
    }
}