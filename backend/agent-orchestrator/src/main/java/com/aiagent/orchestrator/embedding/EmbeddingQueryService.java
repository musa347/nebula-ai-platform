package com.aiagent.orchestrator.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingQueryService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingQueryService.class);
    
    private final EmbeddingService embeddingService;
    private final SemanticSearchService searchService;
    
    public EmbeddingQueryService(EmbeddingService embeddingService, SemanticSearchService searchService) {
        this.embeddingService = embeddingService;
        this.searchService = searchService;
    }
    
    public List<SemanticMatch> query(String queryText, int k) {
        if (queryText == null || queryText.isBlank()) {
            log.warn("Empty query text provided");
            return List.of();
        }
        
        try {
            EmbeddingRequest request = new EmbeddingRequest(queryText, "query");
            EmbeddingResponse response = embeddingService.generateEmbedding(request);
            
            if (response.getEmbedding() == null || response.getEmbedding().isEmpty()) {
                log.warn("Failed to generate query embedding");
                return List.of();
            }
            
            List<SemanticMatch> matches = searchService.search(response.getEmbedding(), k);
            log.info("Query '{}' returned {} matches", queryText, matches.size());
            
            return matches;
            
        } catch (Exception e) {
            log.error("Query failed: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
