package com.aiagent.orchestrator.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    
    private final String ollamaUrl;
    private final String model;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public EmbeddingService(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String ollamaUrl,
            @Value("${spring.ai.ollama.embedding.model:nomic-embed-text}") String model,
            ObjectMapper objectMapper) {
        this.ollamaUrl = ollamaUrl;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(ollamaUrl).build();
        this.objectMapper = objectMapper;
    }
    
    public EmbeddingResponse generateEmbedding(EmbeddingRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            log.warn("Empty content provided for embedding");
            return new EmbeddingResponse(request.getSourceId(), List.of());
        }
        
        try {
            Map<String, String> payload = Map.of(
                "model", model,
                "prompt", request.getContent()
            );
            
            String responseBody = webClient.post()
                    .uri("/api/embeddings")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (responseBody == null) {
                throw new RuntimeException("Empty response from Ollama");
            }
            
            List<Float> embedding = parseEmbedding(responseBody);
            log.debug("Generated embedding for sourceId={}, dimension={}", request.getSourceId(), embedding.size());
            
            return new EmbeddingResponse(request.getSourceId(), embedding);
            
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }
    
    private List<Float> parseEmbedding(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode embeddingNode = root.get("embedding");
        
        if (embeddingNode == null || !embeddingNode.isArray()) {
            throw new RuntimeException("Invalid embedding response format");
        }
        
        List<Float> embedding = new ArrayList<>();
        for (JsonNode value : embeddingNode) {
            embedding.add(value.floatValue());
        }
        
        return embedding;
    }
}
