package com.aiagent.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient mcpWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081") // MCP server base URL
                .build();
    }
}
