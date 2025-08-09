package com.ecommerce.ai_catalog.controller;

import com.ecommerce.ai_catalog.model.Product;
import com.ecommerce.ai_catalog.service.ProductService;
import com.ecommerce.ai_catalog.dto.SearchRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> allProducts() {
        return productService.getAllProducts();
    }

    /**
     * POST /api/search
     * Body: { "query": "running shoes under $100" }
     */
    @PostMapping("/search")
    public Map<String, List<Integer>> search(@RequestBody SearchRequest request) {
        List<Product> matched = productService.smartSearch(request.getQuery());
        List<Integer> ids = matched.stream().map(Product::getId).toList();
        return Map.of("ids", ids);
    }
}
