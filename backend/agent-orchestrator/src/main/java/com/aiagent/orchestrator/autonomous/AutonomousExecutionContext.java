package com.aiagent.orchestrator.autonomous;

import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.ExecutionPlan;
import com.aiagent.common.model.ToolDecision;
import com.aiagent.orchestrator.adaptive.ExecutionRisk;
import com.aiagent.orchestrator.learning.LearningSignal;
import com.aiagent.orchestrator.memory.SemanticMemoryEntry;

import java.util.ArrayList;
import java.util.List;

public class AutonomousExecutionContext {
    private String executionId;
    private String task;
    private ExecutionState currentState;
    private ExecutionPlan plan;
    private List<LearningSignal> signals;
    private List<SemanticMemoryEntry> memories;
    private ExecutionRisk risk;
    private ToolDecision lastToolDecision;
    
    public AutonomousExecutionContext(String executionId, String task) {
        this.executionId = executionId;
        this.task = task;
        this.currentState = ExecutionState.CREATED;
        this.signals = new ArrayList<>();
        this.memories = new ArrayList<>();
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public String getTask() {
        return task;
    }
    
    public ExecutionState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(ExecutionState currentState) {
        this.currentState = currentState;
    }
    
    public ExecutionPlan getPlan() {
        return plan;
    }
    
    public void setPlan(ExecutionPlan plan) {
        this.plan = plan;
    }
    
    public List<LearningSignal> getSignals() {
        return signals;
    }
    
    public void addSignal(LearningSignal signal) {
        if (signal != null) {
            this.signals.add(signal);
        }
    }
    
    public List<SemanticMemoryEntry> getMemories() {
        return memories;
    }
    
    public void addMemory(SemanticMemoryEntry memory) {
        if (memory != null) {
            this.memories.add(memory);
        }
    }
    
    public ExecutionRisk getRisk() {
        return risk;
    }
    
    public void setRisk(ExecutionRisk risk) {
        this.risk = risk;
    }
    
    public ToolDecision getLastToolDecision() {
        return lastToolDecision;
    }
    
    public void setLastToolDecision(ToolDecision lastToolDecision) {
        this.lastToolDecision = lastToolDecision;
    }
}
