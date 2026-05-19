package com.aiagent.cli;

import com.aiagent.orchestrator.streaming.ExecutionEvent;
import com.aiagent.orchestrator.streaming.EventListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class StreamingConsoleRenderer implements EventListener {
    
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private long startTime;
    
    public void start() {
        this.startTime = System.currentTimeMillis();
    }
    
    @Override
    public void onEvent(ExecutionEvent event) {
        if (event == null) {
            return;
        }
        
        String timestamp = TIME_FORMAT.format(new Date(event.getTimestamp()));
        long elapsed = startTime > 0 ? event.getTimestamp() - startTime : 0;
        String elapsedStr = String.format("+%dms", elapsed);
        
        String symbol = getSymbol(event.getType());
        String message = formatMessage(event);
        
        System.out.println(String.format("[%s] %s %s %s", 
            timestamp, elapsedStr, symbol, message));
    }
    
    private String getSymbol(com.aiagent.orchestrator.streaming.ExecutionEventType type) {
        switch (type) {
            case PLAN_CREATED:
                return "[PLAN]";
            case MEMORY_MATCHED:
                return "[MEMORY]";
            case RISK_ANALYZED:
                return "[RISK]";
            case TOOL_SELECTED:
                return "[SELECT]";
            case TOOL_EXECUTED:
                return "[EXEC]";
            case PATCH_GENERATED:
                return "[PATCH]";
            case PATCH_APPLIED:
                return "[APPLY]";
            case LEARNING_UPDATED:
                return "[LEARN]";
            case ERROR:
                return "[ERROR]";
            case COMPLETE:
                return "[DONE]";
            default:
                return "[INFO]";
        }
    }
    
    private String formatMessage(ExecutionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getType()).append(": ");
        sb.append(event.getMessage());
        
        // Add metadata if present
        if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
            sb.append(" ");
            event.getMetadata().forEach((key, value) -> 
                sb.append(key).append("=").append(value).append(" "));
        }
        
        return sb.toString().trim();
    }
}
