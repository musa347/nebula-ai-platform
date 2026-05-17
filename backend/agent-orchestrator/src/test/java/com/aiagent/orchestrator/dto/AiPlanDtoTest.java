package com.aiagent.orchestrator.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPlanDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testAiPlanRequestSerialization() throws Exception {
        // Given
        AiPlanRequest request = new AiPlanRequest("Test task", "{\"steps\":[]}");

        // When
        String json = objectMapper.writeValueAsString(request);
        AiPlanRequest deserialized = objectMapper.readValue(json, AiPlanRequest.class);

        // Then
        assertNotNull(json);
        assertEquals("Test task", deserialized.getTask());
        assertEquals("{\"steps\":[]}", deserialized.getDeterministicPlanJson());
    }

    @Test
    void testAiPlanResponseSerialization() throws Exception {
        // Given
        AiPlanResponse response = new AiPlanResponse("{\"refinedSteps\":[]}");

        // When
        String json = objectMapper.writeValueAsString(response);
        AiPlanResponse deserialized = objectMapper.readValue(json, AiPlanResponse.class);

        // Then
        assertNotNull(json);
        assertEquals("{\"refinedSteps\":[]}", deserialized.getRefinedPlanJson());
    }

    @Test
    void testEmptyConstructors() {
        // Given & When
        AiPlanRequest request = new AiPlanRequest();
        AiPlanResponse response = new AiPlanResponse();

        // Then
        assertNotNull(request);
        assertNotNull(response);
        assertNull(request.getTask());
        assertNull(request.getDeterministicPlanJson());
        assertNull(response.getRefinedPlanJson());
    }
}