/**
 * File: ./api-gateway/src/src/main/java/com/p2pfinance/gateway/controller/FallbackController.java
 */
package com.p2pfinance.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/fallback/user")
    public Mono<ServerResponse> userServiceFallback(ServerRequest request) {
        log.warn("User service fallback triggered: {}", request.path());
        return buildFallbackResponse("User service is temporarily unavailable");
    }

    @RequestMapping("/fallback/wallet")
    public Mono<ServerResponse> walletServiceFallback(ServerRequest request) {
        log.warn("Wallet service fallback triggered: {}", request.path());
        return buildFallbackResponse("Wallet service is temporarily unavailable");
    }

    @RequestMapping("/fallback/payment")
    public Mono<ServerResponse> paymentServiceFallback(ServerRequest request) {
        log.warn("Payment service fallback triggered: {}", request.path());
        return buildFallbackResponse("Payment service is temporarily unavailable");
    }

    @RequestMapping("/fallback/notification")
    public Mono<ServerResponse> notificationServiceFallback(ServerRequest request) {
        log.warn("Notification service fallback triggered: {}", request.path());
        return buildFallbackResponse("Notification service is temporarily unavailable");
    }

    @RequestMapping("/fallback/integration")
    public Mono<ServerResponse> integrationServiceFallback(ServerRequest request) {
        log.warn("Integration service fallback triggered: {}", request.path());
        return buildFallbackResponse("Integration service is temporarily unavailable");
    }

    private Mono<ServerResponse> buildFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("retryAfter", 30); // Suggestion to retry after 30 seconds

        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response));
    }
}