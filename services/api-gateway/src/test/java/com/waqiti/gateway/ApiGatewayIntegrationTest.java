// File: api-gateway/src/test/java/com/waqiti/gateway/ApiGatewayIntegrationTest.java
package com.waqiti.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.gateway.routes[0].id=user_service_route",
                "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/users/**"
        })
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testPublicEndpoint() {
        // Setup WireMock stub for the user service
        stubFor(post(urlEqualTo("/api/v1/users/register"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody("{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"username\":\"testuser\"}")));

        // Test the request through the gateway
        webTestClient.post()
                .uri("/api/v1/users/register")
                .bodyValue("{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("123e4567-e89b-12d3-a456-426614174000")
                .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    void testSecuredEndpoint_Unauthorized() {
        // Setup WireMock stub for a secured endpoint
        stubFor(get(urlEqualTo("/api/v1/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"username\":\"testuser\"}")));

        // Test the request without authentication
        webTestClient.get()
                .uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testSecuredEndpoint_Authorized() {
        // Setup WireMock stub for the auth endpoint
        stubFor(post(urlEqualTo("/api/v1/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"accessToken\":\"valid-token\",\"refreshToken\":\"refresh-token\"}")));

        // Setup WireMock stub for a secured endpoint
        stubFor(get(urlEqualTo("/api/v1/users/me"))
                .withHeader("Authorization", equalTo("Bearer valid-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"username\":\"testuser\"}")));

        // Get a token
        String token = webTestClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue("{\"username\":\"testuser\",\"password\":\"password123\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("valid-token")
                .returnResult()
                .getResponseBody().toString();

        // Test the secured endpoint with the token
        webTestClient.get()
                .uri("/api/v1/users/me")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("123e4567-e89b-12d3-a456-426614174000")
                .jsonPath("$.username").isEqualTo("testuser");
    }
}