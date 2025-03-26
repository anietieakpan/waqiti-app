package com.p2pfinance.integration.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "cyclos")
@Data
@Slf4j
public class CyclosConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String apiKey;
    private boolean useApiKey = false;
    private int connectionTimeout = 60000;

    @Bean
    public WebClient cyclosWebClient() {
        log.info("Initializing Cyclos WebClient with base URL: {}", baseUrl);

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(logRequest())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Add authentication
        if (useApiKey && apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader("Api-Key", apiKey);
        } else {
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        }

        return builder.build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}