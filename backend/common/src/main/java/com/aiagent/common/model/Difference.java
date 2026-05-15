package com.aiagent.common.model;

public class Difference {
    private String type;
    private String nodeType;
    private String messageA;
    private String messageB;

    public Difference() {
    }

    public Difference(String type, String nodeType, String messageA, String messageB) {
        this.type = type;
        this.nodeType = nodeType;
        this.messageA = messageA;
        this.messageB = messageB;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getMessageA() {
        return messageA;
    }

    public void setMessageA(String messageA) {
        this.messageA = messageA;
    }

    public String getMessageB() {
        return messageB;
    }

    public void setMessageB(String messageB) {
        this.messageB = messageB;
    }
}
