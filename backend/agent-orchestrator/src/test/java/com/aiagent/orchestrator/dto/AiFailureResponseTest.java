package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiFailureResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDefaultConstructor() {
        AiFailureResponse response = new AiFailureResponse();
        
        assertNull(response.getFailureType());
        assertNull(response.getRootCause());
        assertNull(response.getSuggestedRecovery());
    }

    @Test
    void testParameterizedConstructor() {
        String failureType = "COMPILATION_ERROR";
        String rootCause = "Missing import statement for CacheService";
        String suggestedRecovery = "Add import: import com.example.CacheService;";
        
        AiFailureResponse response = new AiFailureResponse(failureType, rootCause, suggestedRecovery);
        
        assertEquals(failureType, response.getFailureType());
        assertEquals(rootCause, response.getRootCause());
        assertEquals(suggestedRecovery, response.getSuggestedRecovery());
    }

    @Test
    void testSettersAndGetters() {
        AiFailureResponse response = new AiFailureResponse();
        
        response.setFailureType("RUNTIME_ERROR");
        response.setRootCause("NullPointerException in UserService");
        response.setSuggestedRecovery("Add null check before accessing user object");
        
        assertEquals("RUNTIME_ERROR", response.getFailureType());
        assertEquals("NullPointerException in UserService", response.getRootCause());
        assertEquals("Add null check before accessing user object", response.getSuggestedRecovery());
    }

    @Test
    void testJsonSerialization() throws Exception {
        AiFailureResponse response = new AiFailureResponse(
            "SYNTAX_ERROR",
            "Missing semicolon at line 42",
            "Add semicolon at the end of statement"
        );
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("\"failureType\":\"SYNTAX_ERROR\""));
        assertTrue(json.contains("\"rootCause\":\"Missing semicolon at line 42\""));
        assertTrue(json.contains("\"suggestedRecovery\":\"Add semicolon at the end of statement\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "failureType": "DEPENDENCY_ERROR",
                "rootCause": "Missing dependency: spring-boot-starter-cache",
                "suggestedRecovery": "Add dependency to pom.xml"
            }
            """;
        
        AiFailureResponse response = objectMapper.readValue(json, AiFailureResponse.class);
        
        assertEquals("DEPENDENCY_ERROR", response.getFailureType());
        assertEquals("Missing dependency: spring-boot-starter-cache", response.getRootCause());
        assertEquals("Add dependency to pom.xml", response.getSuggestedRecovery());
    }

    @Test
    void testJsonSerializationWithNullValues() throws Exception {
        AiFailureResponse response = new AiFailureResponse();
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("\"failureType\":null"));
        assertTrue(json.contains("\"rootCause\":null"));
        assertTrue(json.contains("\"suggestedRecovery\":null"));
    }

    @Test
    void testJsonDeserializationWithMissingFields() throws Exception {
        String json = """
            {
                "failureType": "UNKNOWN_ERROR"
            }
            """;
        
        AiFailureResponse response = objectMapper.readValue(json, AiFailureResponse.class);
        
        assertEquals("UNKNOWN_ERROR", response.getFailureType());
        assertNull(response.getRootCause());
        assertNull(response.getSuggestedRecovery());
    }

    @Test
    void testToString() {
        AiFailureResponse response = new AiFailureResponse(
            "CONFIGURATION_ERROR",
            "Invalid configuration in application.yml",
            "Fix YAML syntax and restart application"
        );
        
        String toString = response.toString();
        
        assertTrue(toString.contains("failureType='CONFIGURATION_ERROR'"));
        assertTrue(toString.contains("rootCause='Invalid configuration in application.yml'"));
        assertTrue(toString.contains("suggestedRecovery='Fix YAML syntax and restart application'"));
    }

    @Test
    void testWithComplexRecoveryStrategy() throws Exception {
        String complexRecovery = """
            1. Add missing import statement
            2. Update method signature to match interface
            3. Add @Override annotation
            4. Recompile and test
            """;
        
        AiFailureResponse response = new AiFailureResponse(
            "INTERFACE_MISMATCH",
            "Method signature does not match interface definition",
            complexRecovery
        );
        
        String json = objectMapper.writeValueAsString(response);
        AiFailureResponse deserialized = objectMapper.readValue(json, AiFailureResponse.class);
        
        assertEquals(response.getFailureType(), deserialized.getFailureType());
        assertEquals(response.getRootCause(), deserialized.getRootCause());
        assertEquals(response.getSuggestedRecovery(), deserialized.getSuggestedRecovery());
    }

    @Test
    void testWithSpecialCharacters() throws Exception {
        AiFailureResponse response = new AiFailureResponse(
            "PARSE_ERROR",
            "Unexpected character: '\"' at position 42",
            "Escape special characters: use \\\" instead of \""
        );
        
        String json = objectMapper.writeValueAsString(response);
        AiFailureResponse deserialized = objectMapper.readValue(json, AiFailureResponse.class);
        
        assertEquals(response.getFailureType(), deserialized.getFailureType());
        assertEquals(response.getRootCause(), deserialized.getRootCause());
        assertEquals(response.getSuggestedRecovery(), deserialized.getSuggestedRecovery());
    }

    @Test
    void testWithMultilineContent() throws Exception {
        String multilineRootCause = "Error occurred in multiple locations:\n" +
                                    "1. UserService.java:42\n" +
                                    "2. OrderService.java:15\n" +
                                    "3. PaymentService.java:88";
        
        String multilineRecovery = "Fix each location:\n" +
                                  "- Add null checks\n" +
                                  "- Update method calls\n" +
                                  "- Add error handling";
        
        AiFailureResponse response = new AiFailureResponse(
            "MULTIPLE_ERRORS",
            multilineRootCause,
            multilineRecovery
        );
        
        String json = objectMapper.writeValueAsString(response);
        AiFailureResponse deserialized = objectMapper.readValue(json, AiFailureResponse.class);
        
        assertEquals(response.getRootCause(), deserialized.getRootCause());
        assertEquals(response.getSuggestedRecovery(), deserialized.getSuggestedRecovery());
        assertTrue(deserialized.getRootCause().contains("\n"));
        assertTrue(deserialized.getSuggestedRecovery().contains("\n"));
    }
}