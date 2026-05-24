package com.aiagent.orchestrator.escalation;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeterministicPatchEngine {

    public Optional<String> generatePatch(String task, String fileContent, String filePath) {
        String normalized = task.toLowerCase().trim();

        if (normalized.contains("add log")) {
            return generateLoggingPatch(fileContent, filePath);
        }

        if (normalized.contains("null check")) {
            return generateNullCheckPatch(fileContent);
        }

        if (normalized.contains("timeout")) {
            return generateTimeoutPatch(fileContent);
        }
        return Optional.empty();
    }

    private Optional<String> generateLoggingPatch(String content, String filePath) {
        if (filePath.endsWith(".java")) {
            int methodStart = content.indexOf("public ");
            if (methodStart == -1) methodStart = content.indexOf("private ");
            if (methodStart == -1) return Optional.empty();

            int braceStart = content.indexOf("{", methodStart);
            if (braceStart == -1) return Optional.empty();

            String patch = content.substring(0, braceStart + 1) +
                    "\n        log.info(\"Method execution started\");" +
                    content.substring(braceStart + 1);

            return Optional.of(patch);
        }
        return Optional.empty();
    }

    private Optional<String> generateNullCheckPatch(String content) {
        if (content.contains("public ") && !content.contains("if (") && !content.contains("== null")) {
            return Optional.of(content.replace("public ", "public /* TODO: Add null check */ "));
        }
        return Optional.empty();
    }

    private Optional<String> generateTimeoutPatch(String content) {
        if (content.contains("@RequestMapping") && !content.contains("timeout")) {
            return Optional.of(content.replace("@RequestMapping", "@RequestMapping /* TODO: Add timeout */ "));
        }
        return Optional.empty();
    }

    public double getConfidence(String task) {
        String normalized = task.toLowerCase().trim();

        if (normalized.contains("add log")) return 0.85;
        if (normalized.contains("null check")) return 0.75;
        if (normalized.contains("timeout")) return 0.70;

        return 0.0;
    }
}
