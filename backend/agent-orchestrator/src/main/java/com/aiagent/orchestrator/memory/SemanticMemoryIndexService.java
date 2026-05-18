package com.aiagent.orchestrator.memory;

import com.aiagent.orchestrator.embedding.EmbeddingRequest;
import com.aiagent.orchestrator.embedding.EmbeddingResponse;
import com.aiagent.orchestrator.embedding.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SemanticMemoryIndexService {
    private static final Logger log = LoggerFactory.getLogger(SemanticMemoryIndexService.class);
    
    private final EmbeddingService embeddingService;
    private final SemanticMemoryStore memoryStore;
    
    public SemanticMemoryIndexService(EmbeddingService embeddingService, SemanticMemoryStore memoryStore) {
        this.embeddingService = embeddingService;
        this.memoryStore = memoryStore;
    }
    
    public void indexMemory(SemanticMemoryEntry entry) {
        if (entry == null || entry.getSummary() == null || entry.getSummary().isBlank()) {
            log.warn("Cannot index memory with null or empty summary");
            return;
        }
        
        try {
            EmbeddingRequest request = new EmbeddingRequest(entry.getSummary(), entry.getId());
            EmbeddingResponse response = embeddingService.generateEmbedding(request);
            
            entry.setEmbedding(response.getEmbedding());
            memoryStore.save(entry);
            
            log.info("Indexed memory: id={}, dimension={}", entry.getId(), 
                     response.getEmbedding() != null ? response.getEmbedding().size() : 0);
            
        } catch (Exception e) {
            log.error("Failed to index memory: {}", e.getMessage(), e);
            memoryStore.save(entry);
        }
    }
}
