package com.ecommerce.ai_catalog.dto;

import java.util.List;

public class SearchResponse {
    private List<Integer> ids;
    private String explanation;

    public SearchResponse() {}

    public SearchResponse(List<Integer> ids, String explanation) {
        this.ids = ids;
        this.explanation = explanation;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}

