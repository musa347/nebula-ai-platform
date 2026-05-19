package com.aiagent.orchestrator.streaming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionEventBusTest {
    
    private ExecutionEventBus eventBus;
    
    @BeforeEach
    void setUp() {
        eventBus = new ExecutionEventBus();
    }
    
    @Test
    void testListenersReceiveEvents() {
        List<ExecutionEvent> receivedEvents = new ArrayList<>();
        EventListener listener = receivedEvents::add;
        
        eventBus.subscribe(listener);
        
        ExecutionEvent event = new ExecutionEvent("exec-1", ExecutionEventType.PLAN_CREATED, "Plan created");
        eventBus.publish(event);
        
        assertEquals(1, receivedEvents.size());
        assertEquals(ExecutionEventType.PLAN_CREATED, receivedEvents.get(0).getType());
    }
    
    @Test
    void testUnsubscribeWorks() {
        List<ExecutionEvent> receivedEvents = new ArrayList<>();
        EventListener listener = receivedEvents::add;
        
        eventBus.subscribe(listener);
        eventBus.unsubscribe(listener);
        
        ExecutionEvent event = new ExecutionEvent("exec-1", ExecutionEventType.PLAN_CREATED, "Plan created");
        eventBus.publish(event);
        
        assertTrue(receivedEvents.isEmpty());
    }
    
    @Test
    void testMultipleListenersWork() {
        List<ExecutionEvent> listener1Events = new ArrayList<>();
        List<ExecutionEvent> listener2Events = new ArrayList<>();
        
        EventListener listener1 = listener1Events::add;
        EventListener listener2 = listener2Events::add;
        
        eventBus.subscribe(listener1);
        eventBus.subscribe(listener2);
        
        ExecutionEvent event = new ExecutionEvent("exec-1", ExecutionEventType.TOOL_SELECTED, "Tool selected");
        eventBus.publish(event);
        
        assertEquals(1, listener1Events.size());
        assertEquals(1, listener2Events.size());
        assertEquals(ExecutionEventType.TOOL_SELECTED, listener1Events.get(0).getType());
        assertEquals(ExecutionEventType.TOOL_SELECTED, listener2Events.get(0).getType());
    }
    
    @Test
    void testNullEventIgnored() {
        List<ExecutionEvent> receivedEvents = new ArrayList<>();
        eventBus.subscribe(receivedEvents::add);
        
        eventBus.publish(null);
        
        assertTrue(receivedEvents.isEmpty());
    }
    
    @Test
    void testListenerExceptionDoesNotStopOthers() {
        List<ExecutionEvent> goodListenerEvents = new ArrayList<>();
        
        EventListener badListener = event -> {
            throw new RuntimeException("Bad listener");
        };
        EventListener goodListener = goodListenerEvents::add;
        
        eventBus.subscribe(badListener);
        eventBus.subscribe(goodListener);
        
        ExecutionEvent event = new ExecutionEvent("exec-1", ExecutionEventType.COMPLETE, "Complete");
        eventBus.publish(event);
        
        // Good listener should still receive event
        assertEquals(1, goodListenerEvents.size());
    }
}
