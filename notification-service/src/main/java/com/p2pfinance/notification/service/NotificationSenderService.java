/**
 * File: src/main/java/com/p2pfinance/notification/service/NotificationSenderService.java
 */
package com.p2pfinance.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.p2pfinance.notification.domain.Notification;
import com.p2pfinance.notification.domain.NotificationPreferences;
import com.p2pfinance.notification.repository.NotificationPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// Changed from javax.mail to jakarta.mail
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderService {
    private final NotificationPreferencesRepository preferencesRepository;
    private final JavaMailSender mailSender;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * Sends an email notification
     */
    public boolean sendEmailNotification(Notification notification, String subject, String body) {
        log.info("Sending email notification: {}", notification.getId());

        // Get user's email from preferences
        NotificationPreferences preferences = preferencesRepository.findById(notification.getUserId())
                .orElse(null);

        if (preferences == null || preferences.getEmail() == null || preferences.getEmail().isEmpty()) {
            log.warn("No email address found for user: {}", notification.getUserId());
            return false;
        }

        try {
            // If subject or body is null, use title/message from notification
            if (subject == null) {
                subject = notification.getTitle();
            }

            if (body == null) {
                body = notification.getMessage();
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(preferences.getEmail());
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML content

            mailSender.send(message);

            log.info("Email notification sent successfully: {}", notification.getId());
            return true;
        } catch (Exception e) {
            log.error("Error sending email notification: {}", notification.getId(), e);
            return false;
        }
    }

    /**
     * Sends an SMS notification
     */
    public boolean sendSmsNotification(Notification notification, String smsText) {
        log.info("Sending SMS notification: {}", notification.getId());

        // Get user's phone number from preferences
        NotificationPreferences preferences = preferencesRepository.findById(notification.getUserId())
                .orElse(null);

        if (preferences == null || preferences.getPhoneNumber() == null || preferences.getPhoneNumber().isEmpty()) {
            log.warn("No phone number found for user: {}", notification.getUserId());
            return false;
        }

        try {
            // If smsText is null, use message from notification
            if (smsText == null) {
                smsText = notification.getMessage();
            }

            // TODO: Implement actual SMS service integration
            // For example, using Twilio:
            // Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            // Message message = Message.creator(
            //     new PhoneNumber(preferences.getPhoneNumber()),
            //     new PhoneNumber(FROM_NUMBER),
            //     smsText)
            // .create();

            log.info("SMS notification sent successfully: {}", notification.getId());

            // Simulate success for now
            return true;
        } catch (Exception e) {
            log.error("Error sending SMS notification: {}", notification.getId(), e);
            return false;
        }
    }

    /**
     * Sends a push notification
     */
    public boolean sendPushNotification(Notification notification) {
        log.info("Sending push notification: {}", notification.getId());

        // Get user's device token from preferences
        NotificationPreferences preferences = preferencesRepository.findById(notification.getUserId())
                .orElse(null);

        if (preferences == null || preferences.getDeviceToken() == null || preferences.getDeviceToken().isEmpty()) {
            log.warn("No device token found for user: {}", notification.getUserId());
            return false;
        }

        // If Firebase is not initialized, log and return false
        if (firebaseMessaging == null) {
            log.warn("Firebase Messaging is not initialized. Push notification not sent.");
            return false;
        }

        try {
            // Create data payload
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notification.getId().toString());
            data.put("title", notification.getTitle());
            data.put("body", notification.getMessage());
            data.put("type", notification.getType().toString());
            data.put("category", notification.getCategory());

            if (notification.getReferenceId() != null) {
                data.put("referenceId", notification.getReferenceId());
            }

            if (notification.getActionUrl() != null) {
                data.put("actionUrl", notification.getActionUrl());
            }

            // Create Firebase message, using the fully qualified name for Notification
            Message message = Message.builder()
                    .setToken(preferences.getDeviceToken())
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(notification.getTitle())
                            .setBody(notification.getMessage())
                            .build())
                    .putAllData(data)
                    .build();

            // Send message
            firebaseMessaging.send(message);

            log.info("Push notification sent successfully: {}", notification.getId());
            return true;
        } catch (Exception e) {
            log.error("Error sending push notification: {}", notification.getId(), e);
            return false;
        }
    }
}