/**
 * File: src/test/java/com/waqiti/notification/config/NotificationTestConfig.java
 */
package com.waqiti.notification.config;


import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.persistence.EntityManagerFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Properties;

/**
 * Test configuration for notification service tests
 */
@TestConfiguration
@Profile("test")
@EnableWebSecurity
public class NotificationTestConfig {

    /**
     * Configures transaction manager for JPA
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    /**
     * Configures security for testing
     */
    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure security to make testing easier
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/notifications/send").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Creates a mock JavaMailSender for testing
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
        return mailSender;
    }

    /**
     * Creates a Thymeleaf template engine for testing
     */
    @Bean
    @Primary
    public ITemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    /**
     * Creates a password encoder for testing
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a mock FirebaseMessaging for testing
     * Using a different name to avoid conflicts
     */
    @Bean
    @Qualifier("testFirebaseMessaging")
    public FirebaseMessaging testFirebaseMessaging() {
        return Mockito.mock(FirebaseMessaging.class);
    }

    /**
     * Creates a test message converter for Kafka
     */
    @Bean
    @Primary
    public org.springframework.kafka.support.converter.MessageConverter kafkaMessageConverter() {
        return new org.springframework.kafka.support.converter.StringJsonMessageConverter();
    }

    /**
     * Creates a test Kafka template for testing
     */
    @Bean
    @Primary
    public org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate() {
        return Mockito.mock(org.springframework.kafka.core.KafkaTemplate.class);
    }

    /**
     * Creates a test metrics registry
     */
    @Bean
    @Primary
    public io.micrometer.core.instrument.MeterRegistry meterRegistry() {
        return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
    }
}