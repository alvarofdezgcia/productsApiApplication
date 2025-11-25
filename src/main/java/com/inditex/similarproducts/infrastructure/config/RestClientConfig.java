package com.inditex.similarproducts.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration for REST client and async execution.
 */
@Configuration
public class RestClientConfig {

    @Value("${external.api.timeout.connect:2000}")
    private int connectTimeout;

    @Value("${external.api.timeout.read:5000}")
    private int readTimeout;

    /**
     * Creates a RestTemplate bean with configured timeouts.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }

    /**
     * Creates an Executor for async operations.
     * Uses a fixed thread pool for parallel product fetching.
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
