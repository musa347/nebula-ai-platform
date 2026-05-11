package com.aiagent.mcp.tools.shell.coordinator;

import com.aiagent.common.dto.ExecutionEvent;
import com.aiagent.common.dto.ExecutionEventType;
import com.aiagent.mcp.tools.shell.policy.ShellExecutionPolicy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Coordinates shell execution processes and manages real-time event streaming.
 * Separates concerns from ShellExecutionService by handling process lifecycle,
 * execution tracking, and event broadcasting.
 */
@Component
public class ExecutionCoordinator {
    
    private final ShellExecutionPolicy policy;
    private final ConcurrentMap<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Sinks.Many<ExecutionEvent>> eventStreams = new ConcurrentHashMap<>();
    
    public ExecutionCoordinator(ShellExecutionPolicy policy) {
        this.policy = policy;
    }


    public Flux<ExecutionEvent> executeShellCommand(String command, String workingDirectory) {
        String executionId = UUID.randomUUID().toString();

        Sinks.Many<ExecutionEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();
        eventStreams.put(executionId, eventSink);
        
        return Flux.create(emitter -> {
            try {
                // Validate command
                if (!policy.isAllowed(command)) {
                    ExecutionEvent failedEvent = ExecutionEvent.builder()
                            .executionId(executionId)
                            .type(ExecutionEventType.PROCESS_FAILED)
                            .message("Command not allowed: " + command)
                            .build();
                    emitter.next(failedEvent);
                    emitter.complete();
                    return;
                }

                if (workingDirectory == null || workingDirectory.trim().isEmpty()) {
                    ExecutionEvent failedEvent = ExecutionEvent.builder()
                            .executionId(executionId)
                            .type(ExecutionEventType.PROCESS_FAILED)
                            .message("Working directory cannot be null or empty")
                            .build();
                    emitter.next(failedEvent);
                    emitter.complete();
                    return;
                }
                

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("sh", "-c", command);
                processBuilder.directory(new java.io.File(workingDirectory));
                
                Process process = processBuilder.start();
                runningProcesses.put(executionId, process);
                
                // Emit process started event
                ExecutionEvent startedEvent = ExecutionEvent.builder()
                        .executionId(executionId)
                        .type(ExecutionEventType.PROCESS_STARTED)
                        .message("Process started: " + command)
                        .build();
                emitter.next(startedEvent);
                eventSink.tryEmitNext(startedEvent);
                
                // Stream stdout
                streamProcessOutput(process.getInputStream(), executionId, ExecutionEventType.STDOUT, emitter, eventSink);
                
                // Stream stderr
                streamProcessOutput(process.getErrorStream(), executionId, ExecutionEventType.STDERR, emitter, eventSink);

                boolean completed = process.waitFor(120, TimeUnit.SECONDS);
                
                if (completed) {
                    int exitCode = process.exitValue();
                    if (exitCode == 0) {
                        ExecutionEvent exitEvent = ExecutionEvent.builder()
                                .executionId(executionId)
                                .type(ExecutionEventType.PROCESS_EXIT)
                                .message("Process completed successfully with exit code: " + exitCode)
                                .build();
                        emitter.next(exitEvent);
                        eventSink.tryEmitNext(exitEvent);
                    } else {
                        ExecutionEvent failedEvent = ExecutionEvent.builder()
                                .executionId(executionId)
                                .type(ExecutionEventType.PROCESS_FAILED)
                                .message("Process failed with exit code: " + exitCode)
                                .build();
                        emitter.next(failedEvent);
                        eventSink.tryEmitNext(failedEvent);
                    }
                } else {
                    process.destroyForcibly();
                    ExecutionEvent timeoutEvent = ExecutionEvent.builder()
                            .executionId(executionId)
                            .type(ExecutionEventType.PROCESS_TIMEOUT)
                            .message("Process timed out after 120 seconds and was terminated")
                            .build();
                    emitter.next(timeoutEvent);
                    eventSink.tryEmitNext(timeoutEvent);
                }
                
                emitter.complete();
                
            } catch (Exception e) {
                ExecutionEvent errorEvent = ExecutionEvent.builder()
                        .executionId(executionId)
                        .type(ExecutionEventType.PROCESS_FAILED)
                        .message("Process execution failed: " + e.getMessage())
                        .build();
                emitter.next(errorEvent);
                eventSink.tryEmitNext(errorEvent);
                emitter.error(e);
            } finally {
                // Cleanup
                cleanupExecution(executionId);
            }
        });
    }
    //linebyline
    private void streamProcessOutput(java.io.InputStream inputStream, String executionId, 
                                   ExecutionEventType eventType, 
                                   reactor.core.publisher.FluxSink<ExecutionEvent> emitter,
                                   Sinks.Many<ExecutionEvent> eventSink) {
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ExecutionEvent outputEvent = ExecutionEvent.builder()
                        .executionId(executionId)
                        .type(eventType)
                        .message(line)
                        .build();
                emitter.next(outputEvent);
                eventSink.tryEmitNext(outputEvent);
            }
        } catch (IOException e) {
            ExecutionEvent errorEvent = ExecutionEvent.builder()
                    .executionId(executionId)
                    .type(ExecutionEventType.PROCESS_FAILED)
                    .message("Error reading " + eventType + ": " + e.getMessage())
                    .build();
            emitter.next(errorEvent);
            eventSink.tryEmitNext(errorEvent);
        }
    }

    public boolean cancelExecution(String executionId) {
        Process process = runningProcesses.get(executionId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            ExecutionEvent cancelEvent = ExecutionEvent.builder()
                    .executionId(executionId)
                    .type(ExecutionEventType.PROCESS_FAILED)
                    .message("Process cancelled by user")
                    .build();
            
            Sinks.Many<ExecutionEvent> eventSink = eventStreams.get(executionId);
            if (eventSink != null) {
                eventSink.tryEmitNext(cancelEvent);
            }
            
            cleanupExecution(executionId);
            return true;
        }
        return false;
    }

    public Flux<ExecutionEvent> getEventStream(String executionId) {
        Sinks.Many<ExecutionEvent> eventSink = eventStreams.get(executionId);
        return eventSink != null ? eventSink.asFlux() : Flux.empty();
    }

    private void cleanupExecution(String executionId) {
        runningProcesses.remove(executionId);
        // Keep event stream for a while to allow late subscribers to get the final events
        // Will be cleaned up automatically when no more subscribers exist
    }

    public int getRunningExecutionCount() {
        return runningProcesses.size();
    }
}
