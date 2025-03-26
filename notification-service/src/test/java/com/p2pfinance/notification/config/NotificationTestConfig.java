/**
 * File: src/test/java/com/p2pfinance/notification/config/NotificationTestConfig.java
 */
package com.p2pfinance.notification.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class NotificationTestConfig {

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
     */
    @Bean
    @Primary
    public FirebaseMessaging firebaseMessaging() {
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