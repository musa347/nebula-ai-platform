package com.aiagent.common.model;

import java.util.List;

public class ClassMetadata {
    private String className;
    private List<String> methods;
    private String packageName;

    public ClassMetadata() {}

    public ClassMetadata(String className, List<String> methods, String packageName) {
        this.className = className;
        this.methods = methods;
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
