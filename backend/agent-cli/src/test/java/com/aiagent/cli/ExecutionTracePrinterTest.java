package com.aiagent.cli;

import com.aiagent.common.dto.OrchestratorTaskResponse;
import com.aiagent.common.enums.ExecutionState;
import com.aiagent.common.model.FilePreview;
import com.aiagent.common.model.LoadedContext;
import com.aiagent.common.model.PatchProposal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionTracePrinterTest {
    
    private ExecutionTracePrinter printer;
    private ByteArrayOutputStream outputStream;
    
    @BeforeEach
    void setUp() {
        printer = new ExecutionTracePrinter();
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }
    
    @Test
    void testTracePrintsCorrectly() {
        OrchestratorTaskResponse response = new OrchestratorTaskResponse(
            "exec-123",
            List.of(ExecutionState.CREATED, ExecutionState.PLANNING, ExecutionState.COMPLETED),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        
        printer.print(response);
        
        String output = outputStream.toString();
        assertTrue(output.contains("[PLAN]"));
        assertTrue(output.contains("[TOOL SELECTION]"));
        assertTrue(output.contains("[MEMORY MATCHES]"));
        assertTrue(output.contains("[EXECUTION STEPS]"));
        assertTrue(output.contains("[PATCHES]"));
    }
    
    @Test
    void testIncludesAllOrchLayers() {
        LoadedContext context = new LoadedContext("Service.java", "test");
        FilePreview preview = new FilePreview("Service.java", "line1\nline2\nline3");
        PatchProposal patch = new PatchProposal("Service.java", "Add method", "+ public void newMethod() {}");
        
        OrchestratorTaskResponse response = new OrchestratorTaskResponse(
            "exec-123",
            List.of(ExecutionState.PLANNING, ExecutionState.EXECUTING),
            List.of(context),
            List.of(preview),
            List.of(patch),
            List.of()
        );
        
        printer.print(response);
        
        String output = outputStream.toString();
        assertTrue(output.contains("Service.java"));
        assertTrue(output.contains("3 lines"));
        assertTrue(output.contains("Add method"));
    }
    
    @Test
    void testNoMissingSteps() {
        OrchestratorTaskResponse response = new OrchestratorTaskResponse(
            "exec-123",
            List.of(ExecutionState.CREATED),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        
        printer.print(response);
        
        String output = outputStream.toString();
        // All sections should be present even if empty
        assertTrue(output.contains("[PLAN]"));
        assertTrue(output.contains("[TOOL SELECTION]"));
        assertTrue(output.contains("[MEMORY MATCHES]"));
        assertTrue(output.contains("[EXECUTION STEPS]"));
        assertTrue(output.contains("[PATCHES]"));
    }
}
