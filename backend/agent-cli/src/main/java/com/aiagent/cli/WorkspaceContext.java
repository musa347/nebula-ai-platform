package com.aiagent.cli;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceContext {
    private String rootPath;
    private List<String> sourceFiles;
    private List<String> testFiles;
    private List<String> modules;
    private String buildTool;
    
    public WorkspaceContext() {
        this.sourceFiles = new ArrayList<>();
        this.testFiles = new ArrayList<>();
        this.modules = new ArrayList<>();
    }
    
    public String getRootPath() {
        return rootPath;
    }
    
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    public List<String> getSourceFiles() {
        return sourceFiles;
    }
    
    public void addSourceFile(String file) {
        this.sourceFiles.add(file);
    }
    
    public List<String> getTestFiles() {
        return testFiles;
    }
    
    public void addTestFile(String file) {
        this.testFiles.add(file);
    }
    
    public List<String> getModules() {
        return modules;
    }
    
    public void addModule(String module) {
        this.modules.add(module);
    }
    
    public String getBuildTool() {
        return buildTool;
    }
    
    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }
}
