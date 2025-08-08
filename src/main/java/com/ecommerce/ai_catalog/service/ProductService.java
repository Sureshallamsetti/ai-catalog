package com.ecommerce.ai_catalog.service;

import com.ecommerce.ai_catalog.dto.AIParsedQuery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final List<Map<String, Object>> products;
    private final OpenAIClient openAIClient;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired private OpenAIConfig openAIConfig;

    public ProductService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;

        try (InputStream is = getClass().getResourceAsStream("/products.json")) {
            this.products = mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load products", e);
        }
    }

    public List<Map<String, Object>> getProducts() {
        return products;
    }

    public List<Integer> search(String query) {
        AIParsedQuery parsed = openAIClient.extractSearchParams(query);
        System.out.println("Parsed Query: " + parsed);

        String category = parsed.getCategory() != null ? parsed.getCategory().toLowerCase() : "";
        List<String> keywords = parsed.getKeywords() != null
                ? parsed.getKeywords().stream().map(String::toLowerCase).toList()
                : List.of();
        Double maxPrice = parsed.getMaxPrice();

        System.out.println("Sample product: " + products.get(0));

        return products.stream()
                .filter(p -> {
                    String cat = getString(p.get("category")).toLowerCase();
                    return category.isEmpty() ||
                            cat.contains(category) || category.contains(cat);
                })
                .filter(p -> {
                    if (keywords.isEmpty()) return true;
                    String name = getString(p.get("name")).toLowerCase();
                    String desc = getString(p.get("description")).toLowerCase();
                    return keywords.stream().anyMatch(k ->
                            name.contains(k) || name.contains(k.replaceAll("s$", "")) ||
                                    desc.contains(k) || desc.contains(k.replaceAll("s$", ""))
                    );
                })
                .filter(p -> {
                    if (maxPrice == null) return true;
                    Double price = getDouble(p.get("price"));
                    return price <= maxPrice + 10;
                })
                .map(p -> getInt(p.get("id")))
                .collect(Collectors.toList());
    }



    private String getString(Object value) {
        return value == null ? "" : value.toString();
    }

    private Double getDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }


    // These are unused in your current flow, but left in case you re-integrate OpenAI prompts later
    private String buildPrompt(String query) {
        return String.format("""
                You are an AI product search assistant. Extract search parameters from the user query.
                Respond in JSON format like this:
                {
                  "category": "string or null",
                  "description_keywords": ["keyword1", "keyword2", ...],
                  "max_price": number or null
                }

                User query: "%s"
                """, query);
    }

    private String cleanJsonBlock(String response) {
        if (response == null) return "";
        return response
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();
    }
}
