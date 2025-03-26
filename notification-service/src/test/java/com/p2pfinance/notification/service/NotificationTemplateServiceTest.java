/**
 * File: src/test/java/com/p2pfinance/notification/service/NotificationTemplateServiceTest.java
 */
package com.p2pfinance.notification.service;

import com.p2pfinance.notification.domain.NotificationTemplate;
import com.p2pfinance.notification.dto.NotificationTemplateRequest;
import com.p2pfinance.notification.dto.NotificationTemplateResponse;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private TemplateRenderer templateRenderer;

    @Captor
    private ArgumentCaptor<NotificationTemplate> templateCaptor;

    private NotificationTemplateService templateService;

    private NotificationTemplateRequest templateRequest;
    private NotificationTemplate existingTemplate;

    @BeforeEach
    void setUp() {
        templateService = new NotificationTemplateService(templateRepository, templateRenderer);

        templateRequest = NotificationTemplateRequest.builder()
                .code("test_template")
                .name("Test Template")
                .category("TEST")
                .titleTemplate("Hello ${username}")
                .messageTemplate("This is a test message for ${username}")
                .emailSubjectTemplate("Email subject for ${username}")
                .emailBodyTemplate("<p>Email body for ${username}</p>")
                .smsTemplate("SMS for ${username}")
                .actionUrlTemplate("/action/${username}")
                .enabled(true)
                .build();

        existingTemplate = NotificationTemplate.create(
                "existing_template",
                "Existing Template",
                "EXISTING_CATEGORY",
                "Existing Title ${param}",
                "Existing Message ${param}"
        );
        existingTemplate.setEmailTemplates(
                "Existing Email Subject ${param}",
                "Existing Email Body ${param}"
        );
        existingTemplate.setSmsTemplate("Existing SMS ${param}");
        existingTemplate.setActionUrlTemplate("/existing/${param}");
        // Set a mock ID for the existing template
        try {
            java.lang.reflect.Field idField = NotificationTemplate.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(existingTemplate, UUID.randomUUID());
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
    }

    @Test
    void createTemplate_ShouldCreateNewTemplate() {
        // Given
        when(templateRepository.existsByCode("test_template")).thenReturn(false);
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplateResponse response = templateService.createTemplate(templateRequest);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        assertThat(savedTemplate.getCode()).isEqualTo("test_template");
        assertThat(savedTemplate.getName()).isEqualTo("Test Template");
        assertThat(savedTemplate.getCategory()).isEqualTo("TEST");
        assertThat(savedTemplate.getTitleTemplate()).isEqualTo("Hello ${username}");
        assertThat(savedTemplate.getMessageTemplate()).isEqualTo("This is a test message for ${username}");
        assertThat(savedTemplate.getEmailSubjectTemplate()).isEqualTo("Email subject for ${username}");
        assertThat(savedTemplate.getEmailBodyTemplate()).isEqualTo("<p>Email body for ${username}</p>");
        assertThat(savedTemplate.getSmsTemplate()).isEqualTo("SMS for ${username}");
        assertThat(savedTemplate.getActionUrlTemplate()).isEqualTo("/action/${username}");
        assertThat(savedTemplate.isEnabled()).isTrue();

        assertThat(response.getCode()).isEqualTo("test_template");
    }

    @Test
    void createTemplate_ShouldThrowException_WhenCodeAlreadyExists() {
        // Given
        when(templateRepository.existsByCode("test_template")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(templateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template with code already exists");

        verify(templateRepository, never()).save(any());
    }

    @Test
    void updateTemplate_ShouldUpdateExistingTemplate() {
        // Given
        UUID templateId = existingTemplate.getId();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplateResponse response = templateService.updateTemplate(templateId, templateRequest);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        assertThat(savedTemplate.getTitleTemplate()).isEqualTo("Hello ${username}");
        assertThat(savedTemplate.getMessageTemplate()).isEqualTo("This is a test message for ${username}");
        assertThat(savedTemplate.getEmailSubjectTemplate()).isEqualTo("Email subject for ${username}");
        assertThat(savedTemplate.getEmailBodyTemplate()).isEqualTo("<p>Email body for ${username}</p>");
        assertThat(savedTemplate.getSmsTemplate()).isEqualTo("SMS for ${username}");
        assertThat(savedTemplate.getActionUrlTemplate()).isEqualTo("/action/${username}");
        assertThat(savedTemplate.isEnabled()).isTrue();

        // Verify the response maps correctly
        assertThat(response.getId()).isEqualTo(templateId);
        assertThat(response.getTitleTemplate()).isEqualTo("Hello ${username}");
    }

    @Test
    void updateTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.updateTemplate(templateId, templateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found with ID");

        verify(templateRepository, never()).save(any());
    }

    @Test
    void getTemplateById_ShouldReturnTemplate() {
        // Given
        UUID templateId = existingTemplate.getId();
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));

        // When
        NotificationTemplateResponse response = templateService.getTemplateById(templateId);

        // Then
        assertThat(response.getId()).isEqualTo(templateId);
        assertThat(response.getCode()).isEqualTo("existing_template");
        assertThat(response.getName()).isEqualTo("Existing Template");
        // Verify other fields are mapped correctly
    }

    @Test
    void getTemplateById_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        UUID templateId = UUID.randomUUID();
        when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.getTemplateById(templateId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found with ID");
    }

    @Test
    void getTemplateByCode_ShouldReturnTemplate() {
        // Given
        String code = "existing_template";
        when(templateRepository.findByCode(code)).thenReturn(Optional.of(existingTemplate));

        // When
        NotificationTemplate result = templateService.getTemplateByCode(code);

        // Then
        assertThat(result).isEqualTo(existingTemplate);
    }

    @Test
    void getTemplateByCode_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        String code = "non_existent";
        when(templateRepository.findByCode(code)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.getTemplateByCode(code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Template not found with code");
    }

    @Test
    void getAllTemplates_ShouldReturnAllTemplates() {
        // Given
        List<NotificationTemplate> templates = Arrays.asList(
                NotificationTemplate.create("template1", "Template 1", "CAT1", "Title 1", "Message 1"),
                NotificationTemplate.create("template2", "Template 2", "CAT2", "Title 2", "Message 2")
        );

        when(templateRepository.findAll()).thenReturn(templates);

        // When
        List<NotificationTemplateResponse> responses = templateService.getAllTemplates();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCode()).isEqualTo("template1");
        assertThat(responses.get(1).getCode()).isEqualTo("template2");
    }

    @Test
    void getTemplatesByCategory_ShouldReturnTemplatesInCategory() {
        // Given
        String category = "PAYMENT_REQUEST";
        List<NotificationTemplate> templates = Arrays.asList(
                NotificationTemplate.create("template1", "Template 1", category, "Title 1", "Message 1"),
                NotificationTemplate.create("template2", "Template 2", category, "Title 2", "Message 2")
        );

        when(templateRepository.findByCategory(category)).thenReturn(templates);

        // When
        List<NotificationTemplateResponse> responses = templateService.getTemplatesByCategory(category);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategory()).isEqualTo(category);
        assertThat(responses.get(1).getCategory()).isEqualTo(category);
    }

    @Test
    void setTemplateEnabled_ShouldEnableTemplate() {
        // Given
        UUID templateId = existingTemplate.getId();
        existingTemplate.setEnabled(false);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplateResponse response = templateService.setTemplateEnabled(templateId, true);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        assertThat(savedTemplate.isEnabled()).isTrue();
        assertThat(response.isEnabled()).isTrue();
    }

    @Test
    void setTemplateEnabled_ShouldDisableTemplate() {
        // Given
        UUID templateId = existingTemplate.getId();
        existingTemplate.setEnabled(true);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplateResponse response = templateService.setTemplateEnabled(templateId, false);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        assertThat(savedTemplate.isEnabled()).isFalse();
        assertThat(response.isEnabled()).isFalse();
    }

    @Test
    void renderTemplate_ShouldDelegateToTemplateRenderer() {
        // Given
        String template = "Hello, ${name}!";
        Map<String, Object> model = Collections.singletonMap("name", "John");
        String expected = "Hello, John!";

        when(templateRenderer.renderTemplate(template, model)).thenReturn(expected);

        // When
        String result = templateService.renderTemplate(template, model);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(templateRenderer).renderTemplate(template, model);
    }

    @Test
    void createTemplate_WithNullOptionalFields_ShouldCreateTemplateWithNullFields() {
        // Given
        NotificationTemplateRequest requestWithNulls = NotificationTemplateRequest.builder()
                .code("minimal_template")
                .name("Minimal Template")
                .category("MINIMAL")
                .titleTemplate("Minimal Title")
                .messageTemplate("Minimal Message")
                // Optional fields are null
                .enabled(true)
                .build();

        when(templateRepository.existsByCode("minimal_template")).thenReturn(false);
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplateResponse response = templateService.createTemplate(requestWithNulls);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        assertThat(savedTemplate.getCode()).isEqualTo("minimal_template");
        assertThat(savedTemplate.getEmailSubjectTemplate()).isNull();
        assertThat(savedTemplate.getEmailBodyTemplate()).isNull();
        assertThat(savedTemplate.getSmsTemplate()).isNull();
        assertThat(savedTemplate.getActionUrlTemplate()).isNull();
    }

    @Test
    void updateTemplate_WithPartialFields_ShouldOnlyUpdateProvidedFields() {
        // Given
        UUID templateId = existingTemplate.getId();

        NotificationTemplateRequest partialRequest = NotificationTemplateRequest.builder()
                .code("existing_template") // Same code
                .name("Updated Name")
                .category("EXISTING_CATEGORY") // Same category
                .titleTemplate("Updated Title ${param}")
                .messageTemplate("Updated Message ${param}")
                // Don't update email, SMS templates
                .enabled(true) // Same enabled status
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        templateService.updateTemplate(templateId, partialRequest);

        // Then
        verify(templateRepository).save(templateCaptor.capture());
        NotificationTemplate savedTemplate = templateCaptor.getValue();

        // These should be updated
        assertThat(savedTemplate.getName()).isEqualTo("Updated Name");
        assertThat(savedTemplate.getTitleTemplate()).isEqualTo("Updated Title ${param}");
        assertThat(savedTemplate.getMessageTemplate()).isEqualTo("Updated Message ${param}");

        // These should remain unchanged
        assertThat(savedTemplate.getEmailSubjectTemplate()).isEqualTo("Existing Email Subject ${param}");
        assertThat(savedTemplate.getEmailBodyTemplate()).isEqualTo("Existing Email Body ${param}");
        assertThat(savedTemplate.getSmsTemplate()).isEqualTo("Existing SMS ${param}");
    }
}