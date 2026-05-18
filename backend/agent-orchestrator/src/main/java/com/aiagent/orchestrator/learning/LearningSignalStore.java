package com.aiagent.orchestrator.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LearningSignalStore {
    private static final Logger log = LoggerFactory.getLogger(LearningSignalStore.class);
    
    private final Map<String, LearningSignal> store = new ConcurrentHashMap<>();
    
    public void save(LearningSignal signal) {
        if (signal == null || signal.getExecutionId() == null) {
            throw new IllegalArgumentException("Signal and executionId cannot be null");
        }
        
        store.put(signal.getExecutionId(), signal);
        log.debug("Saved learning signal: executionId={}, outcome={}", 
                  signal.getExecutionId(), signal.getOutcome());
    }
    
    public LearningSignal get(String executionId) {
        return store.get(executionId);
    }
    
    public List<LearningSignal> getAll() {
        return new ArrayList<>(store.values());
    }
    
    public List<LearningSignal> filterByTool(String tool) {
        return store.values().stream()
                .filter(signal -> tool.equals(signal.getToolUsed()))
                .collect(Collectors.toList());
    }
    
    public void clear() {
        store.clear();
        log.debug("Cleared learning signal store");
    }
    
    public int size() {
        return store.size();
    }
}
