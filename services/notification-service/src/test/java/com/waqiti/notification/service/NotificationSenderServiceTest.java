package com.waqiti.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.waqiti.notification.domain.Notification;
import com.waqiti.notification.domain.NotificationPreferences;
import com.waqiti.notification.domain.NotificationType;
import com.waqiti.notification.repository.NotificationPreferencesRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class NotificationSenderServiceTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    private NotificationSenderService senderService;

    private UUID userId;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Create service with direct dependency injection
        senderService = new NotificationSenderService(
                preferencesRepository,
                mailSender,
                firebaseMessaging
        );

        userId = UUID.randomUUID();
        notification = Notification.create(
                userId,
                "Test Notification",
                "This is a test notification message",
                NotificationType.APP,
                "TEST"
        );
        notification.setReferenceId("ref-123");
        notification.setActionUrl("/test/action");
    }

    @Test
    void sendEmailNotification_ShouldSendEmail_WhenEmailAddressExists() throws Exception {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", null, null);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        boolean result = senderService.sendEmailNotification(
                notification, "Test Subject", "Test Body");

        // Then
        assertThat(result).isTrue();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailNotification_ShouldReturnFalse_WhenNoEmailAddress() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        // No email set

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        boolean result = senderService.sendEmailNotification(
                notification, "Test Subject", "Test Body");

        // Then
        assertThat(result).isFalse();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailNotification_ShouldReturnFalse_WhenEmailSendingFails() throws Exception {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", null, null);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Email sending failed")).when(mailSender).send(any(MimeMessage.class));

        // When
        boolean result = senderService.sendEmailNotification(
                notification, "Test Subject", "Test Body");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void sendPushNotification_ShouldSendPushNotification_WhenDeviceTokenExists() throws FirebaseMessagingException {
        // Given
        String deviceToken = "test-device-token";
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);

        // Explicitly enable push notifications and set device token
        preferences.setPushNotificationsEnabled(true);
        preferences.updateContactInfo(null, null, deviceToken);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // Capture the message for verification
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        // Ensure the FirebaseMessaging mock returns a message ID
        when(firebaseMessaging.send(messageCaptor.capture())).thenReturn("message-id-123");

        // When
        boolean result = senderService.sendPushNotification(notification);

        // Then
        assertThat(result).isTrue();

        // Verify the captured message
        Message sentMessage = messageCaptor.getValue();
        assertThat(sentMessage).isNotNull();
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void sendPushNotification_ShouldReturnFalse_WhenNoDeviceToken() throws FirebaseMessagingException {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        // No device token set

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        boolean result = senderService.sendPushNotification(notification);

        // Then
        assertThat(result).isFalse();
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void sendPushNotification_ShouldReturnFalse_WhenFirebaseThrowsException() throws FirebaseMessagingException {
        // Given
        String deviceToken = "test-device-token";
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setPushNotificationsEnabled(true);
        preferences.updateContactInfo(null, null, deviceToken);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // Simulate FirebaseMessagingException
        when(firebaseMessaging.send(any(Message.class)))
                .thenThrow(mock(FirebaseMessagingException.class));

        // When
        boolean result = senderService.sendPushNotification(notification);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void sendSmsNotification_ShouldReturnTrue_WhenPhoneNumberExists() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setSmsNotificationsEnabled(true);
        preferences.updateContactInfo(null, "+1234567890", null);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        boolean result = senderService.sendSmsNotification(notification, "Test SMS");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void sendSmsNotification_ShouldReturnFalse_WhenNoPhoneNumber() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        // No phone number set

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        boolean result = senderService.sendSmsNotification(notification, "Test SMS");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void sendEmailNotification_ShouldUseNotificationTitleAndMessage_WhenSubjectAndBodyAreNull() throws Exception {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", null, null);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        boolean result = senderService.sendEmailNotification(notification, null, null);

        // Then
        assertThat(result).isTrue();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendSmsNotification_ShouldUseNotificationMessage_WhenSmsTextIsNull() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setSmsNotificationsEnabled(true);
        preferences.updateContactInfo(null, "+1234567890", null);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        boolean result = senderService.sendSmsNotification(notification, null);

        // Then
        assertThat(result).isTrue();
    }
}