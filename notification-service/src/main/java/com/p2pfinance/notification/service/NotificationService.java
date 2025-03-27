/**
 * File: src/main/java/com/p2pfinance/notification/service/NotificationService.java
 */
package com.p2pfinance.notification.service;

import com.p2pfinance.notification.domain.*;
import com.p2pfinance.notification.dto.NotificationListResponse;
import com.p2pfinance.notification.dto.NotificationResponse;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    private final NotificationPreferencesService preferencesService;
    private final NotificationSenderService senderService;

    /**
     * Sends a notification using a template
     */
    @Transactional
    public List<NotificationResponse> sendNotification(SendNotificationRequest request) {
        log.info("Sending notification to user: {}, template: {}",
                request.getUserId(), request.getTemplateCode());

        // Get the template
        NotificationTemplate template = templateService.getTemplateByCode(request.getTemplateCode());

        if (!template.isEnabled()) {
            log.warn("Template is disabled: {}", request.getTemplateCode());
            return List.of();
        }

        // Render the templates
        String title = templateService.renderTemplate(template.getTitleTemplate(), request.getParameters());
        String message = templateService.renderTemplate(template.getMessageTemplate(), request.getParameters());

        // Determine which notification types to send based on request and user preferences
        List<NotificationType> typesToSend = determineNotificationTypes(
                request.getUserId(), template.getCategory(), request.getTypes());

        if (typesToSend.isEmpty()) {
            log.info("No notification types enabled for user: {}, template: {}",
                    request.getUserId(), request.getTemplateCode());
            return List.of();
        }

        // Create and send notifications for each type
        List<Notification> notifications = new ArrayList<>();

        for (NotificationType type : typesToSend) {
            // Create the notification
            Notification notification = Notification.create(
                    request.getUserId(), title, message, type, template.getCategory());

            if (request.getReferenceId() != null) {
                notification.setReferenceId(request.getReferenceId());
            }

            if (request.getActionUrl() != null) {
                notification.setActionUrl(request.getActionUrl());
            } else if (template.getActionUrlTemplate() != null) {
                String renderedActionUrl = templateService.renderTemplate(
                        template.getActionUrlTemplate(), request.getParameters());
                notification.setActionUrl(renderedActionUrl);
            }

            if (request.getExpiresAt() != null) {
                notification.setExpiryDate(request.getExpiresAt());
            }

            // Save the initial notification to get an ID
            notification = notificationRepository.save(notification);

            // Process and send the notification
            processNotification(notification, type, template, request.getParameters());

            notifications.add(notification);
        }

        return notifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Process and send a notification
     */
    private void processNotification(Notification notification,
                                     NotificationType type,
                                     NotificationTemplate template,
                                     Map<String, Object> parameters) {
        try {
            boolean sent = false;

            switch (type) {
                case APP -> sent = true; // App notifications are always "sent" as they're stored in DB
                case EMAIL -> sent = senderService.sendEmailNotification(
                        notification,
                        templateService.renderTemplate(template.getEmailSubjectTemplate(), parameters),
                        templateService.renderTemplate(template.getEmailBodyTemplate(), parameters));
                case SMS -> sent = senderService.sendSmsNotification(
                        notification,
                        templateService.renderTemplate(template.getSmsTemplate(), parameters));
                case PUSH -> sent = senderService.sendPushNotification(notification);
            }

            // Update delivery status
            if (sent) {
                notification.updateDeliveryStatus(DeliveryStatus.SENT, null);
            } else {
                notification.updateDeliveryStatus(
                        DeliveryStatus.FAILED, "Failed to send notification");
            }
        } catch (Exception e) {
            log.error("Error sending notification", e);
            notification.updateDeliveryStatus(
                    DeliveryStatus.FAILED, e.getMessage());
        }
    }

    /**
     * Determines which notification types to send based on user preferences
     * Made package-private for testability while maintaining encapsulation
     */
    /* package */ List<NotificationType> determineNotificationTypes(UUID userId,
                                                                    String category,
                                                                    String[] requestedTypes) {
        // Use a mutable list instead of immutable List.of()
        List<NotificationType> enabledTypes = new ArrayList<>();

        // If specific types are requested, use those (still check preferences)
        if (requestedTypes != null && requestedTypes.length > 0) {
            return Arrays.stream(requestedTypes)
                    .map(NotificationType::valueOf)
                    .filter(type -> preferencesService.isNotificationEnabled(userId, category, type))
                    .collect(Collectors.toList());
        }

        // Otherwise, determine based on user preferences
        for (NotificationType type : NotificationType.values()) {
            if (preferencesService.isNotificationEnabled(userId, category, type)) {
                enabledTypes.add(type);
            }
        }

        return enabledTypes;
    }

    /**
     * Gets notifications for a user
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(UUID userId, Pageable pageable) {
        log.info("Getting notifications for user: {}", userId);

        Page<Notification> notificationsPage =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        List<NotificationResponse> notifications = notificationsPage.getContent().stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());

        // Handle unpaged Pageable safely
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : notifications.size();

        return NotificationListResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .totalPages(notificationsPage.getTotalPages())
                .totalElements(notificationsPage.getTotalElements())
                .page(pageNumber)
                .size(pageSize)
                .build();
    }

    /**
     * Gets unread notifications for a user
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getUnreadNotifications(UUID userId, Pageable pageable) {
        log.info("Getting unread notifications for user: {}", userId);

        Page<Notification> notificationsPage =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);

        long unreadCount = notificationsPage.getTotalElements();

        List<NotificationResponse> notifications = notificationsPage.getContent().stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());

        // Handle unpaged Pageable safely
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int pageSize = pageable.isPaged() ? pageable.getPageSize() : notifications.size();

        return NotificationListResponse.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .totalPages(notificationsPage.getTotalPages())
                .totalElements(notificationsPage.getTotalElements())
                .page(pageNumber)
                .size(pageSize)
                .build();
    }

    /**
     * Gets a notification by ID
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(UUID id) {
        log.info("Getting notification with ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));

        return mapToNotificationResponse(notification);
    }

    /**
     * Marks a notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        log.info("Marking notification as read: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        return mapToNotificationResponse(notification);
    }

    /**
     * Marks all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        Page<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId, Pageable.unpaged());

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    /**
     * Scheduled task to retry failed notifications
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    @Transactional
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications");

        List<Notification> failedNotifications =
                notificationRepository.findByDeliveryStatus(DeliveryStatus.FAILED);

        for (Notification notification : failedNotifications) {
            try {
                boolean sent = false;

                switch (notification.getType()) {
                    case APP -> sent = true;
                    case EMAIL -> sent = senderService.sendEmailNotification(notification, null, null);
                    case SMS -> sent = senderService.sendSmsNotification(notification, null);
                    case PUSH -> sent = senderService.sendPushNotification(notification);
                }

                if (sent) {
                    notification.updateDeliveryStatus(DeliveryStatus.SENT, null);
                    notificationRepository.save(notification);
                }
            } catch (Exception e) {
                log.error("Error retrying notification: {}", notification.getId(), e);
            }
        }
    }

    /**
     * Scheduled task to clean up expired notifications
     */
    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("Cleaning up expired notifications");

        List<Notification> expiredNotifications =
                notificationRepository.findByReadFalseAndExpiresAtBeforeAndDeliveryStatus(
                        LocalDateTime.now(), DeliveryStatus.SENT);

        for (Notification notification : expiredNotifications) {
            notification.updateDeliveryStatus(DeliveryStatus.EXPIRED, "Notification expired");
            notificationRepository.save(notification);
        }
    }

    /**
     * Maps a Notification entity to a NotificationResponse DTO
     */
    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().toString())
                .category(notification.getCategory())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt())
                .expiresAt(notification.getExpiresAt())
                .readAt(notification.getReadAt())
                .deliveryStatus(notification.getDeliveryStatus().toString())
                .build();
    }
}