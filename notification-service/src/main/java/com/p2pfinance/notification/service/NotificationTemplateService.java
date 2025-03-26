package com.p2pfinance.notification.service;

import com.p2pfinance.notification.domain.NotificationTemplate;
import com.p2pfinance.notification.dto.NotificationTemplateRequest;
import com.p2pfinance.notification.dto.NotificationTemplateResponse;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {
    private final NotificationTemplateRepository templateRepository;
    private final TemplateRenderer templateRenderer;

    /**
     * Creates a new notification template
     */
    @Transactional
    @CacheEvict(value = "templates", allEntries = true)
    public NotificationTemplateResponse createTemplate(NotificationTemplateRequest request) {
        log.info("Creating notification template with code: {}", request.getCode());

        // Check if code already exists
        if (templateRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Template with code already exists: " + request.getCode());
        }

        // Create the template
        NotificationTemplate template = NotificationTemplate.create(
                request.getCode(),
                request.getName(),
                request.getCategory(),
                request.getTitleTemplate(),
                request.getMessageTemplate()
        );

        // Set optional fields
        if (request.getEmailSubjectTemplate() != null) {
            template.setEmailTemplates(
                    request.getEmailSubjectTemplate(),
                    request.getEmailBodyTemplate()
            );
        }

        if (request.getSmsTemplate() != null) {
            template.setSmsTemplate(request.getSmsTemplate());
        }

        if (request.getActionUrlTemplate() != null) {
            template.setActionUrlTemplate(request.getActionUrlTemplate());
        }

        template.setEnabled(request.isEnabled());

        template = templateRepository.save(template);

        return mapToTemplateResponse(template);
    }

    /**
     * Updates an existing notification template
     */
    @Transactional
    @CacheEvict(value = "templates", allEntries = true)
    public NotificationTemplateResponse updateTemplate(UUID id, NotificationTemplateRequest request) {
        log.info("Updating notification template with ID: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));

        // Update basic fields
        template.updateContent(request.getTitleTemplate(), request.getMessageTemplate());

        // Update optional fields
        if (request.getEmailSubjectTemplate() != null) {
            template.setEmailTemplates(
                    request.getEmailSubjectTemplate(),
                    request.getEmailBodyTemplate()
            );
        }

        if (request.getSmsTemplate() != null) {
            template.setSmsTemplate(request.getSmsTemplate());
        }

        if (request.getActionUrlTemplate() != null) {
            template.setActionUrlTemplate(request.getActionUrlTemplate());
        }

        template.setEnabled(request.isEnabled());

        template = templateRepository.save(template);

        return mapToTemplateResponse(template);
    }

    /**
     * Gets a template by ID
     */
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getTemplateById(UUID id) {
        log.info("Getting notification template with ID: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));

        return mapToTemplateResponse(template);
    }

    /**
     * Gets a template by code
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#code")
    public NotificationTemplate getTemplateByCode(String code) {
        log.info("Getting notification template with code: {}", code);

        return templateRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with code: " + code));
    }

    /**
     * Gets all templates
     */
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getAllTemplates() {
        log.info("Getting all notification templates");

        List<NotificationTemplate> templates = templateRepository.findAll();

        return templates.stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets templates by category
     */
    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> getTemplatesByCategory(String category) {
        log.info("Getting notification templates for category: {}", category);

        List<NotificationTemplate> templates = templateRepository.findByCategory(category);

        return templates.stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Enables or disables a template
     */
    @Transactional
    @CacheEvict(value = "templates", allEntries = true)
    public NotificationTemplateResponse setTemplateEnabled(UUID id, boolean enabled) {
        log.info("Setting template {} to enabled={}", id, enabled);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + id));

        template.setEnabled(enabled);
        template = templateRepository.save(template);

        return mapToTemplateResponse(template);
    }

    /**
     * Renders a template with the given parameters
     */
    public String renderTemplate(String template, Object model) {
        return templateRenderer.renderTemplate(template, model);
    }

    /**
     * Maps a NotificationTemplate entity to a NotificationTemplateResponse DTO
     */
    private NotificationTemplateResponse mapToTemplateResponse(NotificationTemplate template) {
        return NotificationTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .category(template.getCategory())
                .titleTemplate(template.getTitleTemplate())
                .messageTemplate(template.getMessageTemplate())
                .emailSubjectTemplate(template.getEmailSubjectTemplate())
                .emailBodyTemplate(template.getEmailBodyTemplate())
                .smsTemplate(template.getSmsTemplate())
                .actionUrlTemplate(template.getActionUrlTemplate())
                .enabled(template.isEnabled())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}