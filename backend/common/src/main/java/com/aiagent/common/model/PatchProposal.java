package com.aiagent.common.model;

public class PatchProposal {
    private String file;
    private String description;
    private String suggestedChange;

    public PatchProposal() {}

    public PatchProposal(String file, String description, String suggestedChange) {
        this.file = file;
        this.description = description;
        this.suggestedChange = suggestedChange;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuggestedChange() {
        return suggestedChange;
    }

    public void setSuggestedChange(String suggestedChange) {
        this.suggestedChange = suggestedChange;
    }
}