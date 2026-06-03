package com.aiagent.orchestrator.service;

import com.aiagent.common.model.PatchProposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DeterministicPatchGenerator {

    private static final Logger log = LoggerFactory.getLogger(DeterministicPatchGenerator.class);

    public PatchProposal generatePatch(String task, String fileName, String fileContent) {
        String lowerTask = task.toLowerCase();

        if (lowerTask.contains("comment out") || lowerTask.contains("comment-out")) {
            String methodName = extractMethodName(task);
            if (methodName != null) {
                return commentOutMethod(fileName, fileContent, methodName);
            }
        }

        return null;
    }

    private PatchProposal commentOutMethod(String fileName, String fileContent, String methodName) {
        try {
            String modified = commentOutMethodInContent(fileContent, methodName);

            if (modified.equals(fileContent)) {
                log.warn("No changes made - method '{}' not found", methodName);
                return null;
            }

            PatchProposal patch = new PatchProposal();
            patch.setFile(fileName);
            patch.setDescription("Commented out method: " + methodName);
            patch.setSuggestedChange(modified);

            log.info("Generated deterministic patch to comment out method: {}", methodName);
            return patch;

        } catch (Exception e) {
            log.error("Failed to generate deterministic patch", e);
            return null;
        }
    }

    private String commentOutMethodInContent(String content, String methodName) {
        String regex = "(\\s*)((?:public|private|protected)\\s+(?:static\\s+)?(?:final\\s+)?[\\w<>\\[\\],\\s]+\\s+"
                + Pattern.quote(methodName) + "\\s*\\([^)]*\\)\\s*\\{)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        int modificationsCount = 0;

        while (matcher.find()) {
            result.append(content, lastEnd, matcher.start());

            String indent = matcher.group(1);
            int methodStart = matcher.start();
            int braceStart = matcher.end() - 1;
            int braceEnd = findMatchingBrace(content, braceStart);

            if (braceEnd == -1) {
                log.warn("Could not find matching brace for method: {}", methodName);
                result.append(content, matcher.start(), matcher.end());
                lastEnd = matcher.end();
                continue;
            }

            String methodBlock = content.substring(methodStart, braceEnd + 1);
            String commented = commentOutLines(methodBlock, indent);
            result.append(commented);

            lastEnd = braceEnd + 1;
            modificationsCount++;
        }

        result.append(content.substring(lastEnd));

        log.info("Commented out {} occurrence(s) of method: {}", modificationsCount, methodName);
        return result.toString();
    }

    private int findMatchingBrace(String content, int openBracePos) {
        int depth = 1;
        for (int i = openBracePos + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String commentOutLines(String block, String baseIndent) {
        String[] lines = block.split("\n", -1);
        StringBuilder commented = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.trim().isEmpty()) {
                commented.append(line);
            } else {
                String trimmed = line.replaceFirst("^\\s+", "");
                String indent = line.substring(0, line.length() - trimmed.length());
                commented.append(indent).append("// ").append(trimmed);
            }

            if (i < lines.length - 1) {
                commented.append("\n");
            }
        }

        return commented.toString();
    }

    private String extractMethodName(String task) {
        Pattern pattern = Pattern.compile("comment[\\s-]out\\s+(?:the\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s+method", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(task);

        if (matcher.find()) {
            return matcher.group(1);
        }

        pattern = Pattern.compile("comment[\\s-]out\\s+(?:the\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s+in", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(task);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
