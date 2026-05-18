package com.aiagent.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class WorkspaceContextService {
    private static final Logger log = LoggerFactory.getLogger(WorkspaceContextService.class);
    
    public WorkspaceContext scan(String rootPath) {
        WorkspaceContext context = new WorkspaceContext();
        context.setRootPath(rootPath);
        
        File root = new File(rootPath);
        if (!root.exists() || !root.isDirectory()) {
            log.warn("Invalid workspace path: {}", rootPath);
            return context;
        }
        
        // Detect build tool
        detectBuildTool(root, context);
        
        // Scan source files
        scanDirectory(root, "src/main/java", context, true);
        
        // Scan test files
        scanDirectory(root, "src/test/java", context, false);
        
        // Detect modules
        detectModules(root, context);
        
        log.info("Workspace scan complete: {} source files, {} test files, {} modules",
            context.getSourceFiles().size(), context.getTestFiles().size(), context.getModules().size());
        
        return context;
    }
    
    private void detectBuildTool(File root, WorkspaceContext context) {
        if (new File(root, "pom.xml").exists()) {
            context.setBuildTool("maven");
        } else if (new File(root, "build.gradle").exists() || new File(root, "build.gradle.kts").exists()) {
            context.setBuildTool("gradle");
        } else {
            context.setBuildTool("unknown");
        }
    }
    
    private void scanDirectory(File root, String relativePath, WorkspaceContext context, boolean isSource) {
        File dir = new File(root, relativePath);
        if (!dir.exists()) {
            return;
        }
        
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> !p.toString().contains("/target/"))
                 .filter(p -> !p.toString().contains("/build/"))
                 .forEach(p -> {
                     String relative = root.toPath().relativize(p).toString();
                     if (isSource) {
                         context.addSourceFile(relative);
                     } else {
                         context.addTestFile(relative);
                     }
                 });
        } catch (Exception e) {
            log.error("Failed to scan directory: {}", relativePath, e);
        }
    }
    
    private void detectModules(File root, WorkspaceContext context) {
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                // Check if it's a module (has pom.xml or build.gradle)
                if (new File(file, "pom.xml").exists() || 
                    new File(file, "build.gradle").exists()) {
                    context.addModule(file.getName());
                }
            }
        }
    }
}
