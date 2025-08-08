package com.ecommerce.ai_catalog.controller;

import com.ecommerce.ai_catalog.dto.SearchRequest;
import com.ecommerce.ai_catalog.dto.SearchResponse;
import com.ecommerce.ai_catalog.model.Product;
import com.ecommerce.ai_catalog.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // adjust for your dev environment if needed
public class SearchController {
    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String,Object>>> allProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        List<Integer> ids = productService.search(request.getQuery());
        String explanation = "Local matching used";
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            explanation = "Empty query - returning all ids";
        }
        return ResponseEntity.ok(new SearchResponse(ids, explanation));
    }
}
