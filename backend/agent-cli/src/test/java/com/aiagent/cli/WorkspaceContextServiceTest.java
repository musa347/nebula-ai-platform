package com.aiagent.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceContextServiceTest {
    
    private WorkspaceContextService service;
    
    @BeforeEach
    void setUp() {
        service = new WorkspaceContextService();
    }
    
    @Test
    void testDetectsJavaProjectStructure(@TempDir Path tempDir) throws Exception {
        // Create Maven project structure
        Files.createDirectories(tempDir.resolve("src/main/java/com/example"));
        Files.createDirectories(tempDir.resolve("src/test/java/com/example"));
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createFile(tempDir.resolve("src/main/java/com/example/Service.java"));
        Files.createFile(tempDir.resolve("src/test/java/com/example/ServiceTest.java"));
        
        WorkspaceContext context = service.scan(tempDir.toString());
        
        assertEquals("maven", context.getBuildTool());
        assertEquals(1, context.getSourceFiles().size());
        assertEquals(1, context.getTestFiles().size());
        assertTrue(context.getSourceFiles().get(0).contains("Service.java"));
        assertTrue(context.getTestFiles().get(0).contains("ServiceTest.java"));
    }
    
    @Test
    void testHandlesEmptyDirectory(@TempDir Path tempDir) {
        WorkspaceContext context = service.scan(tempDir.toString());
        
        assertNotNull(context);
        assertEquals("unknown", context.getBuildTool());
        assertTrue(context.getSourceFiles().isEmpty());
        assertTrue(context.getTestFiles().isEmpty());
    }
    
    @Test
    void testIgnoresTargetBuild(@TempDir Path tempDir) throws Exception {
        // Create files in target directory (should be ignored)
        Files.createDirectories(tempDir.resolve("target/classes"));
        Files.createDirectories(tempDir.resolve("src/main/java"));
        Files.createFile(tempDir.resolve("target/classes/Generated.java"));
        Files.createFile(tempDir.resolve("src/main/java/Real.java"));
        
        WorkspaceContext context = service.scan(tempDir.toString());
        
        assertEquals(1, context.getSourceFiles().size());
        assertTrue(context.getSourceFiles().get(0).contains("Real.java"));
        assertFalse(context.getSourceFiles().get(0).contains("Generated.java"));
    }
}
