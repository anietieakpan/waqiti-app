package com.waqiti.notification.api;

import com.waqiti.notification.dto.NotificationTemplateRequest;
import com.waqiti.notification.dto.NotificationTemplateResponse;
import com.waqiti.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateController {
    private final NotificationTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody NotificationTemplateRequest request) {
        log.info("Create notification template request received");
        return ResponseEntity.ok(templateService.createTemplate(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationTemplateRequest request) {
        log.info("Update notification template request received for ID: {}", id);
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> getTemplate(@PathVariable UUID id) {
        log.info("Get notification template request received for ID: {}", id);
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationTemplateResponse>> getAllTemplates() {
        log.info("Get all notification templates request received");
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationTemplateResponse>> getTemplatesByCategory(
            @PathVariable String category) {
        log.info("Get notification templates request received for category: {}", category);
        return ResponseEntity.ok(templateService.getTemplatesByCategory(category));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> enableTemplate(@PathVariable UUID id) {
        log.info("Enable notification template request received for ID: {}", id);
        return ResponseEntity.ok(templateService.setTemplateEnabled(id, true));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTemplateResponse> disableTemplate(@PathVariable UUID id) {
        log.info("Disable notification template request received for ID: {}", id);
        return ResponseEntity.ok(templateService.setTemplateEnabled(id, false));
    }
}