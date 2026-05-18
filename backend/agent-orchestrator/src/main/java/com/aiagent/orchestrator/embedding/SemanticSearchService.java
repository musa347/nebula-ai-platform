package com.aiagent.orchestrator.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SemanticSearchService {
    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);
    private static final int MAX_K = 5;
    
    private final EmbeddingStoreService storeService;
    private final CosineSimilarityService similarityService;
    
    public SemanticSearchService(EmbeddingStoreService storeService, CosineSimilarityService similarityService) {
        this.storeService = storeService;
        this.similarityService = similarityService;
    }
    
    public List<SemanticMatch> search(List<Float> queryEmbedding, int k) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return List.of();
        }
        
        k = Math.min(k, MAX_K);
        
        List<EmbeddingVector> allVectors = storeService.findAll();
        if (allVectors.isEmpty()) {
            return List.of();
        }
        
        List<SemanticMatch> matches = new ArrayList<>();
        
        for (EmbeddingVector vector : allVectors) {
            double score = similarityService.calculate(queryEmbedding, vector.getValues());
            matches.add(new SemanticMatch(vector.getId(), score, vector.getSourceType()));
        }
        
        matches.sort(Comparator.comparingDouble(SemanticMatch::getScore).reversed());
        
        List<SemanticMatch> topK = matches.subList(0, Math.min(k, matches.size()));
        log.debug("Semantic search returned {} matches (requested k={})", topK.size(), k);
        
        return topK;
    }
}
