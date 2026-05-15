package com.aiagent.common.model;

public class DependencyEdge {
    private String fromClass;
    private String toClass;
    private String type;

    public DependencyEdge() {}

    public DependencyEdge(String fromClass, String toClass, String type) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.type = type;
    }

    public String getFromClass() {
        return fromClass;
    }

    public void setFromClass(String fromClass) {
        this.fromClass = fromClass;
    }

    public String getToClass() {
        return toClass;
    }

    public void setToClass(String toClass) {
        this.toClass = toClass;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
