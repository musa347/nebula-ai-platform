package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPatchResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDefaultConstructor() {
        AiPatchResponse response = new AiPatchResponse();
        
        assertNull(response.getFile());
        assertNull(response.getDescription());
        assertNull(response.getSuggestedChange());
    }

    @Test
    void testParameterizedConstructor() {
        String file = "UserService.java";
        String description = "Add Redis caching to improve performance";
        String suggestedChange = "@Cacheable(\"users\")\npublic User findById(Long id) { ... }";
        
        AiPatchResponse response = new AiPatchResponse(file, description, suggestedChange);
        
        assertEquals(file, response.getFile());
        assertEquals(description, response.getDescription());
        assertEquals(suggestedChange, response.getSuggestedChange());
    }

    @Test
    void testSettersAndGetters() {
        AiPatchResponse response = new AiPatchResponse();
        
        response.setFile("UserService.java");
        response.setDescription("Fix null pointer exception");
        response.setSuggestedChange("if (user != null) { ... }");
        
        assertEquals("UserService.java", response.getFile());
        assertEquals("Fix null pointer exception", response.getDescription());
        assertEquals("if (user != null) { ... }", response.getSuggestedChange());
    }

    @Test
    void testJsonSerialization() throws Exception {
        AiPatchResponse response = new AiPatchResponse(
            "UserService.java",
            "Add logging for debugging",
            "logger.info(\"Processing user: {}\", userId);"
        );
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("\"file\":\"UserService.java\""));
        assertTrue(json.contains("\"description\":\"Add logging for debugging\""));
        assertTrue(json.contains("\"suggestedChange\":\"logger.info(\\\"Processing user: {}\\\", userId);\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = """
            {
                "file": "UserService.java",
                "description": "Implement validation logic",
                "suggestedChange": "if (!isValid(user)) { throw new ValidationException(); }"
            }
            """;
        
        AiPatchResponse response = objectMapper.readValue(json, AiPatchResponse.class);
        
        assertEquals("UserService.java", response.getFile());
        assertEquals("Implement validation logic", response.getDescription());
        assertEquals("if (!isValid(user)) { throw new ValidationException(); }", response.getSuggestedChange());
    }

    @Test
    void testJsonSerializationWithNullValues() throws Exception {
        AiPatchResponse response = new AiPatchResponse();
        
        String json = objectMapper.writeValueAsString(response);
        
        assertTrue(json.contains("\"file\":null"));
        assertTrue(json.contains("\"description\":null"));
        assertTrue(json.contains("\"suggestedChange\":null"));
    }

    @Test
    void testJsonDeserializationWithMissingFields() throws Exception {
        String json = """
            {
                "file": "UserService.java"
            }
            """;
        
        AiPatchResponse response = objectMapper.readValue(json, AiPatchResponse.class);
        
        assertEquals("UserService.java", response.getFile());
        assertNull(response.getDescription());
        assertNull(response.getSuggestedChange());
    }

    @Test
    void testToString() {
        AiPatchResponse response = new AiPatchResponse(
            "TestFile.java",
            "Test description",
            "public void testMethod() { System.out.println(\"test\"); }"
        );
        
        String toString = response.toString();
        
        assertTrue(toString.contains("file='TestFile.java'"));
        assertTrue(toString.contains("description='Test description'"));
        assertTrue(toString.contains("chars")); // suggestedChange length
    }

    @Test
    void testToStringWithNullSuggestedChange() {
        AiPatchResponse response = new AiPatchResponse(
            "TestFile.java",
            "Test description",
            null
        );
        
        String toString = response.toString();
        
        assertTrue(toString.contains("suggestedChange='null'"));
    }

    @Test
    void testComplexSuggestedChange() throws Exception {
        String complexChange = """
            @Service
            public class UserService {
                
                @Autowired
                private UserRepository userRepository;
                
                @Cacheable("users")
                public User findById(Long id) {
                    return userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id));
                }
            }
            """;
        
        AiPatchResponse response = new AiPatchResponse(
            "UserService.java",
            "Add caching and error handling",
            complexChange
        );
        
        // Test serialization/deserialization with complex content
        String json = objectMapper.writeValueAsString(response);
        AiPatchResponse deserialized = objectMapper.readValue(json, AiPatchResponse.class);
        
        assertEquals(response.getFile(), deserialized.getFile());
        assertEquals(response.getDescription(), deserialized.getDescription());
        assertEquals(response.getSuggestedChange(), deserialized.getSuggestedChange());
    }
}