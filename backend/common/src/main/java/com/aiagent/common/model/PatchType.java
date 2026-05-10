package com.aiagent.common.model;

/**
 * Types of patch operations.
 */
public enum PatchType {

    REPLACE("replace"),
    INSERT("insert"),
    DELETE("delete");
    
    private final String operation;
    
    PatchType(String operation) {
        this.operation = operation;
    }
    
    public String getOperation() {
        return operation;
    }
}
