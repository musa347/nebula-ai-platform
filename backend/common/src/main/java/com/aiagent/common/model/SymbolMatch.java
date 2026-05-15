package com.aiagent.common.model;

public class SymbolMatch {
    private String className;
    private String methodName;
    private String file;

    public SymbolMatch() {}

    public SymbolMatch(String className, String methodName, String file) {
        this.className = className;
        this.methodName = methodName;
        this.file = file;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
