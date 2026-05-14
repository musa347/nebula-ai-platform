package com.aiagent.mcp.tools.git.checkpoint;

import com.aiagent.common.model.GitCheckpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GitCheckpointManager {

    private static final Logger log = LoggerFactory.getLogger(GitCheckpointManager.class);
    private final Map<String, GitCheckpoint> checkpoints = new ConcurrentHashMap<>();

    public GitCheckpoint createCheckpoint(String workspacePath, String sessionId, String description) throws IOException {
        String checkpointId = UUID.randomUUID().toString();

        ProcessBuilder pb = new ProcessBuilder("git", "stash", "push", "-u", "-m", "checkpoint:" + checkpointId);
        pb.directory(Path.of(workspacePath).toFile());

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            log.info("Checkpoint created: {}", output.toString());
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to create checkpoint");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Checkpoint creation interrupted", e);
        }

        GitCheckpoint checkpoint = GitCheckpoint.builder()
                .checkpointId(checkpointId)
                .sessionId(sessionId)
                .stashId("stash@{0}")
                .modifiedFiles(List.of())
                .createdAt(LocalDateTime.now())
                .description(description)
                .build();

        checkpoints.put(checkpointId, checkpoint);
        log.info("Checkpoint {} created for session {}", checkpointId, sessionId);

        return checkpoint;
    }

    public void restoreCheckpoint(String workspacePath, String checkpointId) throws IOException {
        GitCheckpoint checkpoint = checkpoints.get(checkpointId);
        if (checkpoint == null) {
            throw new IllegalArgumentException("Checkpoint not found: " + checkpointId);
        }

        // Find the stash index by checkpoint ID
        String stashRef = findStashByCheckpointId(workspacePath, checkpointId);
        if (stashRef == null) {
            throw new IOException("Stash not found for checkpoint: " + checkpointId);
        }

        ProcessBuilder pb = new ProcessBuilder("git", "stash", "apply", stashRef);
        pb.directory(Path.of(workspacePath).toFile());

        Process process = pb.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to restore checkpoint");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Checkpoint restore interrupted", e);
        }

        checkpoints.remove(checkpointId);
        log.info("Checkpoint {} restored", checkpointId);
    }

    private String findStashByCheckpointId(String workspacePath, String checkpointId) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("git", "stash", "list");
        pb.directory(Path.of(workspacePath).toFile());

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("checkpoint:" + checkpointId)) {
                    return line.split(":")[0];
                }
            }
        }

        return null;
    }

    public void dropCheckpoint(String workspacePath, String checkpointId) throws IOException {
        GitCheckpoint checkpoint = checkpoints.get(checkpointId);
        if (checkpoint == null) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("git", "stash", "drop");
        pb.directory(Path.of(workspacePath).toFile());

        Process process = pb.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Checkpoint drop interrupted", e);
        }

        checkpoints.remove(checkpointId);
        log.info("Checkpoint {} dropped", checkpointId);
    }

    public GitCheckpoint getCheckpoint(String checkpointId) {
        return checkpoints.get(checkpointId);
    }

    public List<GitCheckpoint> getCheckpointsBySession(String sessionId) {
        return checkpoints.values().stream()
                .filter(cp -> sessionId.equals(cp.getSessionId()))
                .toList();
    }
}
