package com.aiagent.common.dto;

import com.aiagent.common.model.ContextSlice;

import java.util.List;

public class ContextResponse {
    private List<ContextSlice> slices;

    public ContextResponse() {}

    public ContextResponse(List<ContextSlice> slices) {
        this.slices = slices;
    }

    public List<ContextSlice> getSlices() {
        return slices;
    }

    public void setSlices(List<ContextSlice> slices) {
        this.slices = slices;
    }
}
