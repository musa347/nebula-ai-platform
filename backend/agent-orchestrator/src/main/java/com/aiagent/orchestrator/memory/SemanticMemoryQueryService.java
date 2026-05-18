package com.aiagent.orchestrator.memory;

import com.aiagent.orchestrator.embedding.CosineSimilarityService;
import com.aiagent.orchestrator.embedding.EmbeddingRequest;
import com.aiagent.orchestrator.embedding.EmbeddingResponse;
import com.aiagent.orchestrator.embedding.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SemanticMemoryQueryService {
    private static final Logger log = LoggerFactory.getLogger(SemanticMemoryQueryService.class);
    private static final int MAX_K = 5;
    
    private final EmbeddingService embeddingService;
    private final SemanticMemoryStore memoryStore;
    private final CosineSimilarityService similarityService;
    
    public SemanticMemoryQueryService(EmbeddingService embeddingService, 
                                      SemanticMemoryStore memoryStore,
                                      CosineSimilarityService similarityService) {
        this.embeddingService = embeddingService;
        this.memoryStore = memoryStore;
        this.similarityService = similarityService;
    }
    
    public List<MemoryMatch> query(String taskQuery, int k) {
        if (taskQuery == null || taskQuery.isBlank()) {
            log.warn("Empty task query provided");
            return List.of();
        }
        
        k = Math.min(k, MAX_K);
        
        try {
            EmbeddingRequest request = new EmbeddingRequest(taskQuery, "query");
            EmbeddingResponse response = embeddingService.generateEmbedding(request);
            
            if (response.getEmbedding() == null || response.getEmbedding().isEmpty()) {
                return List.of();
            }
            
            List<SemanticMemoryEntry> allMemories = memoryStore.findAll();
            if (allMemories.isEmpty()) {
                return List.of();
            }
            
            List<MemoryMatch> matches = new ArrayList<>();
            
            for (SemanticMemoryEntry memory : allMemories) {
                if (memory.getEmbedding() == null || memory.getEmbedding().isEmpty()) {
                    continue;
                }
                
                double score = similarityService.calculate(response.getEmbedding(), memory.getEmbedding());
                matches.add(new MemoryMatch(memory.getId(), score, memory.getOutcome()));
            }
            
            matches.sort(Comparator.comparingDouble(MemoryMatch::getScore).reversed());
            
            List<MemoryMatch> topK = matches.subList(0, Math.min(k, matches.size()));
            log.info("Memory query '{}' returned {} matches", taskQuery, topK.size());
            
            return topK;
            
        } catch (Exception e) {
            log.error("Memory query failed: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
