package com.ecommerce.ai_catalog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Value("${openai.api.key:}")
    private String key;

    public String getKey() { return key; }
}
