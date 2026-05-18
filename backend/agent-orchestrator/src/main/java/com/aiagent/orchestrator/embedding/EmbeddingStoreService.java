package com.aiagent.orchestrator.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmbeddingStoreService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingStoreService.class);
    
    private final Map<String, EmbeddingVector> store = new ConcurrentHashMap<>();
    
    public void save(EmbeddingVector embedding) {
        if (embedding == null || embedding.getId() == null) {
            throw new IllegalArgumentException("Embedding and ID cannot be null");
        }
        
        store.put(embedding.getId(), embedding);
        log.debug("Saved embedding: id={}, dimension={}", embedding.getId(), 
                  embedding.getValues() != null ? embedding.getValues().size() : 0);
    }
    
    public EmbeddingVector get(String id) {
        return store.get(id);
    }
    
    public List<EmbeddingVector> findAll() {
        return new ArrayList<>(store.values());
    }
    
    public void clear() {
        store.clear();
        log.debug("Cleared embedding store");
    }
    
    public int size() {
        return store.size();
    }
}
