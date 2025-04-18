package com.waqiti.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.cloud.gateway.routes[0].id=test-route",
                "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**"})
@AutoConfigureWireMock(port = 0)
class TracingFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldAddCorrelationIdToRequest() {
        // Given
        stubFor(get(urlEqualTo("/test/endpoint"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("X-Correlation-ID", "{{request.headers.X-Correlation-ID}}")
                        .withBody("Test response")));

        // When
        webTestClient.get().uri("/test/endpoint")
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().exists("X-Correlation-ID")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String correlationId = response.getResponseHeaders().getFirst("X-Correlation-ID");
                    assertThat(correlationId).isNotEmpty();
                });

        // Verify the request reached wiremock with the correlation ID
        verify(getRequestedFor(urlEqualTo("/test/endpoint"))
                .withHeader("X-Correlation-ID", matching("[a-f0-9\\-]+")));
    }

    @Test
    void shouldPreserveExistingCorrelationId() {
        // Given
        String existingCorrelationId = "existing-correlation-id";

        stubFor(get(urlEqualTo("/test/endpoint"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("X-Correlation-ID", "{{request.headers.X-Correlation-ID}}")
                        .withBody("Test response")));

        // When
        webTestClient.get().uri("/test/endpoint")
                .header("X-Correlation-ID", existingCorrelationId)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-ID", existingCorrelationId)
                .expectBody(String.class);

        // Verify the request reached wiremock with the original correlation ID
        verify(getRequestedFor(urlEqualTo("/test/endpoint"))
                .withHeader("X-Correlation-ID", equalTo(existingCorrelationId)));
    }
}