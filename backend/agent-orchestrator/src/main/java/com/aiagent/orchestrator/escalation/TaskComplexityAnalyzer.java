package com.aiagent.orchestrator.escalation;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TaskComplexityAnalyzer {

    private static final Set<String> SIMPLE_KEYWORDS = Set.of(
            "add logging", "add log", "add cache", "add caching",
            "add validation", "add timeout", "rename", "add comment",
            "fix typo", "update version", "add null check"
    );

    private static final Set<String> ARCHITECTURE_KEYWORDS = Set.of(
            "architecture", "redesign", "migrate", "microservice",
            "event sourcing", "cqrs", "saga", "distributed"
    );

    private static final Pattern SINGLE_FILE_PATTERN = Pattern.compile(
            ".*\\b(in|to|at)\\s+\\w+\\.(java|kt|py|js|ts)\\b.*",
            Pattern.CASE_INSENSITIVE
    );

    public TaskComplexity analyze(String task) {
        String normalized = task.toLowerCase().trim();

        if (ARCHITECTURE_KEYWORDS.stream().anyMatch(normalized::contains)) {
            return TaskComplexity.COMPLEX;
        }
        if (SIMPLE_KEYWORDS.stream().anyMatch(normalized::contains)) {
            if (normalized.split("\\s+").length < 8) {
                return TaskComplexity.SIMPLE;
            }
        }

        if (SINGLE_FILE_PATTERN.matcher(normalized).matches()) {
            return TaskComplexity.SIMPLE;
        }

        if (normalized.split("\\s+").length > 15) {
            return TaskComplexity.COMPLEX;
        }
        return TaskComplexity.MODERATE;
    }
}
