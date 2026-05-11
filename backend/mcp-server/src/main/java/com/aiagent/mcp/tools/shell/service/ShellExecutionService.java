package com.aiagent.mcp.tools.shell.service;

import com.aiagent.common.dto.ShellExecuteRequest;
import com.aiagent.common.dto.ShellExecuteResponse;
import com.aiagent.mcp.tools.shell.policy.ShellExecutionPolicy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Service
public class ShellExecutionService {

    private final ShellExecutionPolicy policy;

    public ShellExecutionService(ShellExecutionPolicy policy) {
        this.policy = policy;
    }

    public ShellExecuteResponse execute(ShellExecuteRequest request) {
        if (!policy.isAllowed(request.getCommand())) {
            return ShellExecuteResponse.builder()
                    .success(false)
                    .exitCode(-1)
                    .stderr("Command not allowed: " + request.getCommand())
                    .durationMs(0L)
                    .build();
        }

        return runProcess(request);
    }

    private ShellExecuteResponse runProcess(ShellExecuteRequest request) {
        long start = System.currentTimeMillis();
        Process process = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c", request.getCommand());

            // Set working directory
            if (request.getWorkingDirectory() != null && !request.getWorkingDirectory().trim().isEmpty()) {
                File workingDir = new File(request.getWorkingDirectory());
                if (workingDir.exists() && workingDir.isDirectory()) {
                    processBuilder.directory(workingDir);
                } else {
                    return ShellExecuteResponse.builder()
                            .success(false)
                            .exitCode(-1)
                            .stderr("Invalid working directory: " + request.getWorkingDirectory())
                            .durationMs(System.currentTimeMillis() - start)
                            .build();
                }
            }

            process = processBuilder.start();

            boolean completed = process.waitFor(120, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                return ShellExecuteResponse.builder()
                        .success(false)
                        .exitCode(-1)
                        .stderr("Process timeout after 120 seconds")
                        .durationMs(System.currentTimeMillis() - start)
                        .build();
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.exitValue();

            return ShellExecuteResponse.builder()
                    .success(exitCode == 0)
                    .stdout(stdout)
                    .stderr(stderr)
                    .exitCode(exitCode)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();

        } catch (Exception e) {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            
            return ShellExecuteResponse.builder()
                    .success(false)
                    .exitCode(-1)
                    .stderr("Execution error: " + e.getMessage())
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
