package com.aiagent.orchestrator.retry;

import com.aiagent.common.enums.FailureType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class FailureClassifier {
    private final Map<FailureType, Predicate<String>> patterns;

    public FailureClassifier() {
        patterns = Map.of(
                FailureType.SECURITY_VIOLATION,
                s -> s.contains("permission denied") || s.contains("access denied"),
                FailureType.INVALID_COMMAND,
                s -> s.contains("command not found") || s.contains("invalid command"),
                FailureType.TIMEOUT,
                s -> s.contains("time out") || s.contains("timeout"),
                FailureType.COMPILATION_FAILURE,
                s -> s.contains("compilation error") || s.contains("syntax error"),
                FailureType.MISSING_IMPORT,
                s -> s.contains("missing import") || s.contains("import error"),
                FailureType.ASSERTION_FAILURE,
                s -> s.contains("assertion failed") || s.contains("assertion error"),
                FailureType.TEST_FAILURE,
                s -> s.contains("test failed") || s.contains("test error")

        );
    }

    public FailureType classify(String output) {
        if (output == null) return FailureType.UNKNOWN;
        String lower = output.toLowerCase();

        return patterns.entrySet().stream()
                .filter(entry -> entry.getValue().test(lower))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(FailureType.UNKNOWN);
    }
}
