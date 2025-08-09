package com.ecommerce.ai_catalog.service;

import com.ecommerce.ai_catalog.dto.AIParsedQuery;
import com.ecommerce.ai_catalog.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.FuzzyScore;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;
@Service
public class ProductService {
    private final List<Product> products;
    private final OpenAIClient ai;
    private final FuzzyScore fuzzy = new FuzzyScore(Locale.ENGLISH);

    private final Map<String,List<String>> synonyms = Map.of(
            "bike", List.of("bicycle","cycle","bike"),
            "shoes", List.of("shoes","sneakers","running shoes","trainers")
    );

    public ProductService(OpenAIClient ai) {
        this.ai = ai;
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/products.json")) {
            this.products = mapper.readValue(is, new TypeReference<List<Product>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load products", e);
        }
    }

    public List<Product> getAllProducts() { return products; }

    public List<Product> smartSearch(String query) {
        if (query == null) query = "";
        String normalizedQuery = query.trim();

        // Ask AI for parsed parameters
        AIParsedQuery parsed = ai.extractSearchParams(normalizedQuery);

        // Fallback: if AI returned empty, derive keywords from query
        boolean parsedEmpty = ( (parsed.getKeywords() == null || parsed.getKeywords().isEmpty())
                && parsed.getCategory() == null && parsed.getMinPrice() == null && parsed.getMaxPrice() == null );

        List<String> keywords;
        if (parsedEmpty) {
            keywords = Arrays.stream(normalizedQuery.split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } else {
            keywords = parsed.getKeywords() == null ? new ArrayList<>() :
                    parsed.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList());
        }

        // Expand synonyms
        Set<String> expanded = new HashSet<>(keywords);
        for (String k : new ArrayList<>(keywords)) {
            for (String key : synonyms.keySet()) {
                if (key.equals(k) || synonyms.get(key).contains(k)) expanded.addAll(synonyms.get(key));
            }
        }

        String categoryFilter = parsed.getCategory() == null ? "" : parsed.getCategory().toLowerCase();
        BigDecimal minPrice = parsed.getMinPrice() == null ? null : BigDecimal.valueOf(parsed.getMinPrice());
        BigDecimal maxPrice = parsed.getMaxPrice() == null ? null : BigDecimal.valueOf(parsed.getMaxPrice());

        // Filter
        List<Product> filtered = products.stream()
                .filter(p -> {
                    // category filter
                    if (!categoryFilter.isBlank()) {
                        String cat = p.getCategory() == null ? "" : p.getCategory().toLowerCase();
                        if (!cat.contains(categoryFilter) && !categoryFilter.contains(cat)) return false;
                    }

                    // price filter
                    BigDecimal price = p.getPrice();
                    if (price == null) return false;
                    if (minPrice != null && price.compareTo(minPrice) < 0) return false;
                    if (maxPrice != null && price.compareTo(maxPrice) > 0) return false;

                    // keywords: if none requested, accept
                    if (expanded.isEmpty()) return true;

                    String name = p.getName() == null ? "" : p.getName().toLowerCase();
                    String desc = p.getDescription() == null ? "" : p.getDescription().toLowerCase();

                    return expanded.stream().anyMatch(k -> name.contains(k) || desc.contains(k));
                })
                .collect(Collectors.toList());

        // Sort by fuzzy score vs query text, tiebreaker by rating desc
        filtered.sort((a,b) -> {
            int scoreA = fuzzy.fuzzyScore(a.getName() == null ? "" : a.getName().toLowerCase(), normalizedQuery.toLowerCase());
            int scoreB = fuzzy.fuzzyScore(b.getName() == null ? "" : b.getName().toLowerCase(), normalizedQuery.toLowerCase());
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            Double ra = a.getRating() == null ? 0.0 : a.getRating();
            Double rb = b.getRating() == null ? 0.0 : b.getRating();
            return Double.compare(rb, ra);
        });

        return filtered;
    }
}