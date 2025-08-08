package com.ecommerce.ai_catalog.service;

import com.ecommerce.ai_catalog.dto.AIParsedQuery;
import com.ecommerce.ai_catalog.service.OpenAIConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OpenAIClient {

    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    public AIParsedQuery String;

    @Autowired
    public OpenAIClient(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    public String ask(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.getKey());

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[]{
                        Map.of("role", "system", "content",
                                "You are a helpful AI that extracts product search parameters in strict JSON format. Respond only with JSON keys: category (string), keywords (array of strings), maxPrice (number). No explanations."),
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.2
        );


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            var choices = (java.util.List<Map<String, Object>>) response.getBody().get("choices");
            var message = (Map<String, Object>) choices.get(0).get("message");
            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public AIParsedQuery extractSearchParams(String query) {
        String prompt = """
You are an AI assistant for an e-commerce product catalog.

The catalog contains products in these categories: 
Running, Hiking, Casual, Cycling, Apparel, Accessories, Fitness, Swimming

Your job is to extract the user's **intent** into:
- category
- keywords (as list)
- maxPrice (if specified)

Respond only in this JSON format:

{
  "category": "Cycling",
  "keywords": ["helmet", "lightweight"],
  "maxPrice": 100.0
}

If any field is missing from the query, set it to null or an empty list.

User Query: "%s"
""".formatted(query);


        String response = ask(prompt);

        try {
            // Parse the JSON response from OpenAI
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.readValue(response, AIParsedQuery.class));
            return objectMapper.readValue(response, AIParsedQuery.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new AIParsedQuery(); // empty fallback
        }
    }

}
