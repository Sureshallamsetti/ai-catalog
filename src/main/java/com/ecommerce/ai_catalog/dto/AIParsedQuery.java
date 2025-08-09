package com.ecommerce.ai_catalog.dto;

import java.util.List;

public class AIParsedQuery {
    private String category;
    private List<String> keywords;
    private Double minPrice;
    private Double maxPrice;

    public AIParsedQuery() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    @Override
    public String toString() {
        return "AIParsedQuery{" +
                "category='" + category + '\'' +
                ", keywords=" + keywords +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                '}';
    }
}
