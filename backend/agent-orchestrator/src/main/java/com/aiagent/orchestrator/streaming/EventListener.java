package com.aiagent.orchestrator.streaming;

public interface EventListener {
    void onEvent(ExecutionEvent event);
}
