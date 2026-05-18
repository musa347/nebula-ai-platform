package com.aiagent.orchestrator.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SemanticMemoryStore {
    private static final Logger log = LoggerFactory.getLogger(SemanticMemoryStore.class);
    
    private final Map<String, SemanticMemoryEntry> store = new ConcurrentHashMap<>();
    
    public void save(SemanticMemoryEntry entry) {
        if (entry == null || entry.getId() == null) {
            throw new IllegalArgumentException("Entry and ID cannot be null");
        }
        
        store.put(entry.getId(), entry);
        log.debug("Saved memory entry: id={}, executionId={}, outcome={}", 
                  entry.getId(), entry.getExecutionId(), entry.getOutcome());
    }
    
    public SemanticMemoryEntry get(String id) {
        return store.get(id);
    }
    
    public List<SemanticMemoryEntry> getByExecutionId(String executionId) {
        return store.values().stream()
                .filter(entry -> executionId.equals(entry.getExecutionId()))
                .collect(Collectors.toList());
    }
    
    public List<SemanticMemoryEntry> findAll() {
        return new ArrayList<>(store.values());
    }
    
    public void clear() {
        store.clear();
        log.debug("Cleared semantic memory store");
    }
    
    public int size() {
        return store.size();
    }
}
