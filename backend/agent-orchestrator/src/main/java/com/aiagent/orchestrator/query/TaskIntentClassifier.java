package com.aiagent.orchestrator.query;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TaskIntentClassifier {

    private static final Set<String> ANALYSIS_KEYWORDS = Set.of(
            "explain", "overview", "what does", "how does", "describe",
            "summarize", "summary", "understand", "read", "show me",
            "tell me about", "analyze", "review", "examine", "inspect",
            "what is", "how is", "why does", "walk through", "breakdown"
    );

    private static final Set<String> MODIFICATION_KEYWORDS = Set.of(
            "add", "remove", "delete", "update", "modify", "change",
            "refactor", "fix", "implement", "create", "generate",
            "improve", "optimize", "replace", "rename", "move"
    );

    public TaskIntent classify(String task) {
        String normalized = task.toLowerCase().trim();

        for (String keyword : MODIFICATION_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return TaskIntent.MODIFICATION;
            }
        }

        for (String keyword : ANALYSIS_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return TaskIntent.ANALYSIS;
            }
        }

        if (normalized.startsWith("what") || normalized.startsWith("how") ||
                normalized.startsWith("why") || normalized.startsWith("where") ||
                normalized.endsWith("?")) {
            return TaskIntent.ANALYSIS;
        }

        return TaskIntent.MODIFICATION;
    }
}
