/**
 * File: ./api-gateway/src/src/main/java/com/p2pfinance/gateway/config/RouteConfig.java
 */
package com.p2pfinance.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteConfig {

    @Value("${services.user-service.url}")
    private String userServiceUrl;

    @Value("${services.wallet-service.url}")
    private String walletServiceUrl;

    @Value("${services.payment-service.url}")
    private String paymentServiceUrl;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Value("${services.integration-service.url}")
    private String integrationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            @Qualifier("defaultRedisRateLimiter") RedisRateLimiter defaultRateLimiter,
            @Qualifier("ipRedisRateLimiter") RedisRateLimiter ipRateLimiter,
            @Qualifier("userRedisRateLimiter") RedisRateLimiter userRateLimiter,
            @Qualifier("authRedisRateLimiter") RedisRateLimiter authRateLimiter,
            @Qualifier("ipKeyResolver") KeyResolver ipKeyResolver,
            @Qualifier("userKeyResolver") KeyResolver userKeyResolver,
            @Qualifier("compositeKeyResolver") KeyResolver compositeKeyResolver) {

        return builder.routes()
                // Authentication routes (with stricter rate limiting)
                .route("auth_route", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(authRateLimiter)
                                        .setKeyResolver(ipKeyResolver)))
                        .uri(userServiceUrl))

                // User registration route (with IP-based rate limiting)
                .route("user_registration_route", r -> r
                        .path("/api/v1/users/register")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(ipRateLimiter)
                                        .setKeyResolver(ipKeyResolver)))
                        .uri(userServiceUrl))

                // Public verification routes (with IP-based rate limiting)
                .route("user_verification_route", r -> r
                        .path("/api/v1/users/verify/**", "/api/v1/users/password/reset/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(ipRateLimiter)
                                        .setKeyResolver(ipKeyResolver)))
                        .uri(userServiceUrl))

                // User service routes (authenticated, with user-based rate limiting)
                .route("user_service_route", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(compositeKeyResolver)))
                        .uri(userServiceUrl))

                // Wallet service routes (authenticated, with user-based rate limiting)
                .route("wallet_service_route", r -> r
                        .path("/api/v1/wallets/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("walletServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/wallet")))
                        .uri(walletServiceUrl))

                // Payment request routes
                .route("payment_request_route", r -> r
                        .path("/api/v1/payment-requests/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment")))
                        .uri(paymentServiceUrl))

                // Scheduled payment routes
                .route("scheduled_payment_route", r -> r
                        .path("/api/v1/scheduled-payments/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment")))
                        .uri(paymentServiceUrl))

                // Split payment routes
                .route("split_payment_route", r -> r
                        .path("/api/v1/split-payments/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment")))
                        .uri(paymentServiceUrl))

                // Notification routes
                .route("notification_route", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification")))
                        .uri(notificationServiceUrl))

                // Integration routes (admin only - can be disabled in production)
                .route("integration_route", r -> r
                        .path("/api/v1/integration/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(userRateLimiter)
                                        .setKeyResolver(userKeyResolver))
                                .circuitBreaker(c -> c
                                        .setName("integrationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/integration")))
                        .uri(integrationServiceUrl))

                // OpenAPI documentation routes
                .route("swagger_ui_route", r -> r
                        .path("/swagger-ui/**")
                        .uri(userServiceUrl))
                .route("api_docs_route", r -> r
                        .path("/v3/api-docs/**")
                        .uri(userServiceUrl))

                // Health check routes
                .route("actuator_health_route", r -> r
                        .path("/actuator/health/**", "/actuator/info/**")
                        .uri("lb://actuator"))

                .build();
    }
}