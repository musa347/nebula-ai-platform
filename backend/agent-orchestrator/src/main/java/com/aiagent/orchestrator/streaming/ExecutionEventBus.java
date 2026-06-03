package com.aiagent.orchestrator.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExecutionEventBus {
    private static final Logger log = LoggerFactory.getLogger(ExecutionEventBus.class);
    
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
    
    public void subscribe(EventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.debug("Listener subscribed. Total listeners: {}", listeners.size());
        }
    }
    
    public void unsubscribe(EventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            log.debug("Listener unsubscribed. Total listeners: {}", listeners.size());
        }
    }
    
    public void publish(ExecutionEvent event) {
        if (event == null) {
            return;
        }
        
        log.debug("Publishing event: type={}, executionId={}", event.getType(), event.getExecutionId());
        
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", e.getMessage(), e);
            }
        }
    }
    
    public int getListenerCount() {
        return listeners.size();
    }
    
    public void clear() {
        listeners.clear();
        log.debug("All listeners cleared");
    }
}
