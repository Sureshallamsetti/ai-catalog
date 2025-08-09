package com.ecommerce.ai_catalog.service;

import com.ecommerce.ai_catalog.dto.AIParsedQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OpenAIClient {
    private final OpenAIConfig cfg;
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Autowired
    public OpenAIClient(OpenAIConfig cfg) { this.cfg = cfg; }

    public AIParsedQuery extractSearchParams(String userQuery) {
        try {
            String prompt = buildPrompt(userQuery);
            String raw = callOpenAI(prompt);
            if (raw == null || raw.isBlank()) return new AIParsedQuery();

            raw = cleanRawJson(raw);
            AIParsedQuery parsed = mapper.readValue(raw, AIParsedQuery.class);
            if (parsed.getKeywords() == null) parsed.setKeywords(Collections.emptyList());
            return parsed;
        } catch (Exception e) {
            e.printStackTrace();
            return new AIParsedQuery();
        }
    }

    private String buildPrompt(String q) {
        return String.format(
                "You must respond ONLY with a single JSON object (no markdown, no explanation). " +
                        "Fields: category (Running,Hiking,Casual,Cycling,Apparel,Accessories,Fitness,Swimming or null), " +
                        "keywords (array), minPrice (number or null), maxPrice (number or null). " +
                        "If user says 'under $X' set maxPrice = X. If user says 'around $X' set minPrice = X*0.8 and maxPrice = X*1.2. " +
                        "If price is not present use nulls. " +
                        "User Query: \"%s\"", q
        );
    }

    @SuppressWarnings("unchecked")
    private String callOpenAI(String prompt) throws Exception {
        HttpHeaders hd = new HttpHeaders();
        hd.setContentType(MediaType.APPLICATION_JSON);
        hd.setBearerAuth(cfg.getKey());

        Map<String,Object> system = Map.of("role","system","content","You are a JSON-only response assistant.");
        Map<String,Object> user = Map.of("role","user","content", prompt);

        Map<String,Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("messages", new Object[]{system, user});
        body.put("temperature", 0.0);

        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, hd);
        ResponseEntity<Map> resp = rest.exchange(OPENAI_URL, HttpMethod.POST, req, Map.class);

        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) return null;
        var choices = (List<Map<String,Object>>) resp.getBody().get("choices");
        if (choices == null || choices.isEmpty()) return null;
        var message = (Map<String,Object>) choices.get(0).get("message");
        if (message == null) return null;
        return String.valueOf(message.get("content"));
    }

    private String cleanRawJson(String raw) {
        raw = raw == null ? "" : raw.strip();
        if (raw.startsWith("```")) {
            int firstNewline = raw.indexOf('\n');
            if (firstNewline > 0 && firstNewline + 1 < raw.length()) raw = raw.substring(firstNewline + 1);
            if (raw.endsWith("```")) raw = raw.substring(0, raw.length()-3);
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) raw = raw.substring(start, end+1);
        return raw.strip();
    }
}
