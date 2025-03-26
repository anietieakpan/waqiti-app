/**
 * File: src/test/java/com/p2pfinance/notification/repository/NotificationTemplateRepositoryTest.java
 */
package com.p2pfinance.notification.repository;

import com.p2pfinance.notification.TestcontainersBase;
import com.p2pfinance.notification.domain.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use real DB from Testcontainers
@ActiveProfiles("test")
@Tag("IntegrationTest")
class NotificationTemplateRepositoryTest extends TestcontainersBase {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        templateRepository.deleteAll();

        // Create some test templates
        createTemplate("payment_request_created", "Payment Request Created", "PAYMENT_REQUEST", true);
        createTemplate("payment_request_approved", "Payment Request Approved", "PAYMENT_REQUEST", true);
        createTemplate("payment_request_rejected", "Payment Request Rejected", "PAYMENT_REQUEST", false);
        createTemplate("wallet_deposit", "Wallet Deposit", "TRANSACTION", true);
        createTemplate("wallet_withdrawal", "Wallet Withdrawal", "TRANSACTION", true);
        createTemplate("security_login", "Security Login", "SECURITY", true);
    }

    private NotificationTemplate createTemplate(String code, String name, String category, boolean enabled) {
        NotificationTemplate template = NotificationTemplate.create(
                code,
                name,
                category,
                "Title template for " + name,
                "Message template for " + name
        );
        template.setEnabled(enabled);
        return templateRepository.save(template);
    }

    @Test
    void findByCode_ShouldReturnTemplateWithMatchingCode() {
        // When
        Optional<NotificationTemplate> result = templateRepository.findByCode("payment_request_created");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("payment_request_created");
        assertThat(result.get().getName()).isEqualTo("Payment Request Created");
    }

    @Test
    void findByCategory_ShouldReturnTemplatesWithMatchingCategory() {
        // When
        List<NotificationTemplate> paymentRequestTemplates = templateRepository.findByCategory("PAYMENT_REQUEST");
        List<NotificationTemplate> transactionTemplates = templateRepository.findByCategory("TRANSACTION");

        // Then
        assertThat(paymentRequestTemplates).hasSize(3);
        assertThat(transactionTemplates).hasSize(2);

        for (NotificationTemplate template : paymentRequestTemplates) {
            assertThat(template.getCategory()).isEqualTo("PAYMENT_REQUEST");
        }
    }

    @Test
    void findByEnabledTrue_ShouldReturnOnlyEnabledTemplates() {
        // When
        List<NotificationTemplate> enabledTemplates = templateRepository.findByEnabledTrue();

        // Then
        assertThat(enabledTemplates).hasSize(5); // Only 5 templates are enabled

        for (NotificationTemplate template : enabledTemplates) {
            assertThat(template.isEnabled()).isTrue();
        }
    }

    @Test
    void findByCategoryAndEnabledTrue_ShouldFilterByCategoryAndEnabled() {
        // When
        List<NotificationTemplate> enabledPaymentRequestTemplates =
                templateRepository.findByCategoryAndEnabledTrue("PAYMENT_REQUEST");

        // Then
        assertThat(enabledPaymentRequestTemplates).hasSize(2); // Out of 3 PAYMENT_REQUEST templates, 2 are enabled

        for (NotificationTemplate template : enabledPaymentRequestTemplates) {
            assertThat(template.getCategory()).isEqualTo("PAYMENT_REQUEST");
            assertThat(template.isEnabled()).isTrue();
        }
    }

    @Test
    void existsByCode_ShouldReturnTrueWhenCodeExists() {
        // When & Then
        assertThat(templateRepository.existsByCode("payment_request_created")).isTrue();
        assertThat(templateRepository.existsByCode("non_existent_code")).isFalse();
    }

    @Test
    void saveAndFind_ShouldPersistAllTemplateFields() {
        // Given
        NotificationTemplate template = NotificationTemplate.create(
                "test_template",
                "Test Template",
                "TEST",
                "Test Title ${param}",
                "Test Message ${param}"
        );
        template.setEmailTemplates(
                "Test Email Subject ${param}",
                "<p>Test Email Body ${param}</p>"
        );
        template.setSmsTemplate("Test SMS ${param}");
        template.setActionUrlTemplate("/test/${param}");

        // When
        NotificationTemplate savedTemplate = templateRepository.save(template);
        NotificationTemplate foundTemplate = templateRepository.findById(savedTemplate.getId()).orElseThrow();

        // Then
        assertThat(foundTemplate.getCode()).isEqualTo("test_template");
        assertThat(foundTemplate.getName()).isEqualTo("Test Template");
        assertThat(foundTemplate.getCategory()).isEqualTo("TEST");
        assertThat(foundTemplate.getTitleTemplate()).isEqualTo("Test Title ${param}");
        assertThat(foundTemplate.getMessageTemplate()).isEqualTo("Test Message ${param}");
        assertThat(foundTemplate.getEmailSubjectTemplate()).isEqualTo("Test Email Subject ${param}");
        assertThat(foundTemplate.getEmailBodyTemplate()).isEqualTo("<p>Test Email Body ${param}</p>");
        assertThat(foundTemplate.getSmsTemplate()).isEqualTo("Test SMS ${param}");
        assertThat(foundTemplate.getActionUrlTemplate()).isEqualTo("/test/${param}");
        assertThat(foundTemplate.isEnabled()).isTrue();
        assertThat(foundTemplate.getCreatedAt()).isNotNull();
        assertThat(foundTemplate.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateTemplate_ShouldUpdateFields() {
        // Given
        NotificationTemplate template = templateRepository.findByCode("payment_request_created").orElseThrow();

        // When
        template.updateContent("New Title Template", "New Message Template");
        template.setEmailTemplates("New Email Subject", "New Email Body");
        template.setSmsTemplate("New SMS Template");
        template.setActionUrlTemplate("/new/action/${id}");
        template.setEnabled(false);

        NotificationTemplate updatedTemplate = templateRepository.save(template);

        // Then
        NotificationTemplate foundTemplate = templateRepository.findById(updatedTemplate.getId()).orElseThrow();

        assertThat(foundTemplate.getTitleTemplate()).isEqualTo("New Title Template");
        assertThat(foundTemplate.getMessageTemplate()).isEqualTo("New Message Template");
        assertThat(foundTemplate.getEmailSubjectTemplate()).isEqualTo("New Email Subject");
        assertThat(foundTemplate.getEmailBodyTemplate()).isEqualTo("New Email Body");
        assertThat(foundTemplate.getSmsTemplate()).isEqualTo("New SMS Template");
        assertThat(foundTemplate.getActionUrlTemplate()).isEqualTo("/new/action/${id}");
        assertThat(foundTemplate.isEnabled()).isFalse();
    }

    @Test
    void findNonExistentTemplate_ShouldReturnEmpty() {
        // When
        Optional<NotificationTemplate> result = templateRepository.findByCode("non_existent_template");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteTemplate_ShouldRemoveTemplate() {
        // Given
        NotificationTemplate template = templateRepository.findByCode("payment_request_created").orElseThrow();

        // When
        templateRepository.delete(template);

        // Then
        assertThat(templateRepository.findById(template.getId())).isEmpty();
        assertThat(templateRepository.findByCode("payment_request_created")).isEmpty();
    }

    @Test
    void findAllTemplates_ShouldReturnAllTemplates() {
        // When
        List<NotificationTemplate> allTemplates = templateRepository.findAll();

        // Then
        assertThat(allTemplates).hasSize(6); // We have 6 templates in total
    }

    @Test
    void updateTimestamps_ShouldUpdateUpdatedAtField() {
        // Given
        NotificationTemplate template = templateRepository.findByCode("payment_request_created").orElseThrow();
        LocalDateTime initialUpdatedAt = template.getUpdatedAt();

        // Small delay to ensure timestamp difference
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // When
        template.updateContent("New Title", "New Message");
        NotificationTemplate updatedTemplate = templateRepository.save(template);

        // Then
        assertThat(updatedTemplate.getUpdatedAt()).isAfter(initialUpdatedAt);
    }
}