/**
 * File: src/test/java/com/waqiti/notification/TestcontainersBase.java
 */
package com.waqiti.notification;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for Testcontainers-based tests.
 * Provides PostgreSQL and Kafka containers that will be shared across all test classes.
 */
@Testcontainers
public abstract class TestcontainersBase {

    /**
     * PostgreSQL container for test database
     */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
                    .withDatabaseName("notification_test_db")
                    .withUsername("notification_test")
                    .withPassword("notification_test")
                    .withReuse(true)
                    .waitingFor(Wait.forListeningPort());

    /**
     * Kafka container for test message broker
     */
    @Container
    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                    .withReuse(true)
                    .waitingFor(Wait.forListeningPort());

    /**
     * Configure Spring Boot to use the Testcontainers-provided instances
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway properties (use the correct database)
        registry.add("spring.flyway.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRES_CONTAINER::getPassword);

        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);

        // Log the container information for debugging
        System.out.println("PostgreSQL container started at: " + POSTGRES_CONTAINER.getJdbcUrl());
        System.out.println("Kafka container started at: " + KAFKA_CONTAINER.getBootstrapServers());
    }
}