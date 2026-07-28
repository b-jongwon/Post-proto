package com.facthub.factcheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

    @Bean(name = "geminiRestClient")
    public RestClient geminiRestClient() {
        return RestClient
                .builder()
                .baseUrl(
                        "https://generativelanguage.googleapis.com"
                )
                .build();
    }
}