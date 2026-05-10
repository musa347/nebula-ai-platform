package com.aiagent.common.model;

/**
 * Represents a patch operation for file modifications.
 */
public class PatchOperation {

    private PatchType type;
    private String filePath;
    private Integer startLine;
    private Integer endLine;
    private String oldContent;
    private String newContent;
    private boolean reverse = false;
    private String description;
    

    public PatchOperation() {}

    public PatchOperation(PatchType type, String filePath, Integer startLine, Integer endLine,
                        String oldContent, String newContent, boolean reverse, String description) {
        this.type = type;
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.reverse = reverse;
        this.description = description;
    }

    public PatchType getType() { return type; }
    public void setType(PatchType type) { this.type = type; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public Integer getStartLine() { return startLine; }
    public void setStartLine(Integer startLine) { this.startLine = startLine; }
    
    public Integer getEndLine() { return endLine; }
    public void setEndLine(Integer endLine) { this.endLine = endLine; }
    
    public String getOldContent() { return oldContent; }
    public void setOldContent(String oldContent) { this.oldContent = oldContent; }
    
    public String getNewContent() { return newContent; }
    public void setNewContent(String newContent) { this.newContent = newContent; }
    
    public boolean isReverse() { return reverse; }
    public void setReverse(boolean reverse) { this.reverse = reverse; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    

    public boolean isValid() {
        return type != null && filePath != null && !filePath.trim().isEmpty();
    }

    public boolean isReplacement() {
        return PatchType.REPLACE.equals(type);
    }

    public boolean isInsertion() {
        return PatchType.INSERT.equals(type);
    }

    public boolean isDeletion() {
        return PatchType.DELETE.equals(type);
    }

    public static PatchOperation replace(String filePath, Integer startLine, Integer endLine, 
                                       String oldContent, String newContent) {
        return new PatchOperation(PatchType.REPLACE, filePath, startLine, endLine, oldContent, newContent, false, null);
    }

    public static PatchOperation insert(String filePath, Integer line, String content) {
        return new PatchOperation(PatchType.INSERT, filePath, line, line, null, content, false, null);
    }

    public static PatchOperation delete(String filePath, Integer startLine, Integer endLine) {
        return new PatchOperation(PatchType.DELETE, filePath, startLine, endLine, null, null, false, null);
    }
}
