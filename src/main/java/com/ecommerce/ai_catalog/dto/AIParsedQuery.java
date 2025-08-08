package com.ecommerce.ai_catalog.dto;

import java.util.List;

public class AIParsedQuery {
    private String category;
    private List<String> keywords;
    private Double maxPrice;

    public AIParsedQuery() {
        // Default constructor needed by Jackson
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
}


