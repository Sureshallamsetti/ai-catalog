package com.ecommerce.ai_catalog;

import com.ecommerce.ai_catalog.service.OpenAIConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCatalogApplication.class, args);
	}

}
