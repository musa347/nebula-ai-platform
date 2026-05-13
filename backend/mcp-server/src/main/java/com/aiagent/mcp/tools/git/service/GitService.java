package com.aiagent.mcp.tools.git.service;

import com.aiagent.common.dto.GitDiffResponse;
import com.aiagent.common.dto.GitRestoreResponse;
import com.aiagent.common.dto.GitStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitService {
    private static final Logger log = LoggerFactory.getLogger(GitService.class);
    private final GitExecutionPolicy policy;

    public GitService(GitExecutionPolicy policy) {
        this.policy = policy;
    }

    public GitStatusResponse getStatus(String workspacePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("git", "status", "--porcelain", "-b");
        pb.directory(Path.of(workspacePath).toFile());
        Process process = pb.start();
        List<String> modified = new ArrayList<>();
        List<String> untracked = new ArrayList<>();
        String branch = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) {
                    branch = parseBranch(line);
                } else if (line.startsWith("??")) {
                    untracked.add(line.substring(3).trim());
                } else if (line.length() > 3) {
                    modified.add(line.substring(3).trim());
                }
            }
        }
        return GitStatusResponse.builder().modifiedFiles(modified).untrackedFiles(untracked).currentBranch(branch).build();
    }

    public GitDiffResponse getDiff(String workspacePath, List<String> files) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("diff");
        if (files != null && !files.isEmpty()) {
            command.add("--");
            command.addAll(files);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Path.of(workspacePath).toFile());
        Process process = pb.start();
        StringBuilder diff = new StringBuilder();
        List<String> affected = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                diff.append(line).append("\n");
                if (line.startsWith("diff --git")) {
                    String file = extractFilename(line);
                    if (file != null) {
                        affected.add(file);
                    }
                }
            }
        }
        return GitDiffResponse.builder().diff(diff.toString()).affectedFiles(affected).build();
    }

    public GitRestoreResponse restore(String workspacePath, List<String> files) throws IOException {
        if (files != null) {
            for (String file : files) {
                policy.validateRestoreTarget(file);
            }
        }
        List<String> command = new ArrayList<>();
        command.add("git");
        command.add("restore");
        if (files == null || files.isEmpty()) {
            command.add(".");
        } else {
            command.add("--");
            command.addAll(files);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Path.of(workspacePath).toFile());
        Process process = pb.start();
        try {
            int exitCode = process.waitFor();
            return GitRestoreResponse.builder().restoredFiles(files != null ? files : List.of(".")).success(exitCode == 0).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Restore interrupted", e);
        }
    }

    private String parseBranch(String line) {
        Pattern pattern = Pattern.compile("## ([^.\\s]+)");
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private String extractFilename(String diffLine) {
        Pattern pattern = Pattern.compile("b/(.+)$");
        Matcher matcher = pattern.matcher(diffLine);
        return matcher.find() ? matcher.group(1) : null;
    }
}
