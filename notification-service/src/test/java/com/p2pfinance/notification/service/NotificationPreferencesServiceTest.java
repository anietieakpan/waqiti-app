/**
 * File: src/test/java/com/p2pfinance/notification/service/NotificationPreferencesServiceTest.java
 */
package com.p2pfinance.notification.service;

import com.p2pfinance.notification.domain.NotificationPreferences;
import com.p2pfinance.notification.domain.NotificationType;
import com.p2pfinance.notification.dto.NotificationPreferencesResponse;
import com.p2pfinance.notification.dto.UpdatePreferencesRequest;
import com.p2pfinance.notification.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class NotificationPreferencesServiceTest {

    @Mock
    private NotificationPreferencesRepository preferencesRepository;

    @Captor
    private ArgumentCaptor<NotificationPreferences> preferencesCaptor;

    private NotificationPreferencesService preferencesService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        preferencesService = new NotificationPreferencesService(preferencesRepository);
        userId = UUID.randomUUID();
    }

    @Test
    void getOrCreatePreferences_ShouldReturnExistingPreferences() {
        // Given
        NotificationPreferences existingPreferences = NotificationPreferences.createDefault(userId);
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPreferences));

        // When
        NotificationPreferences result = preferencesService.getOrCreatePreferences(userId);

        // Then
        assertThat(result).isSameAs(existingPreferences);
        verify(preferencesRepository, never()).save(any());
    }

    @Test
    void getOrCreatePreferences_ShouldCreateDefaultPreferencesWhenNotFound() {
        // Given
        when(preferencesRepository.findById(userId)).thenReturn(Optional.empty());
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationPreferences result = preferencesService.getOrCreatePreferences(userId);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        assertThat(savedPreferences.getUserId()).isEqualTo(userId);
        assertThat(savedPreferences.isAppNotificationsEnabled()).isTrue();
        assertThat(savedPreferences.isEmailNotificationsEnabled()).isTrue();
        assertThat(savedPreferences.isSmsNotificationsEnabled()).isFalse();
        assertThat(savedPreferences.isPushNotificationsEnabled()).isFalse();
    }

    @Test
    void updatePreferences_ShouldUpdateAllProvidedFields() {
        // Given
        NotificationPreferences existingPreferences = NotificationPreferences.createDefault(userId);
        Map<String, Boolean> categoryPreferences = new HashMap<>();
        categoryPreferences.put("PAYMENT_REQUEST", false);

        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
                .appNotificationsEnabled(true)
                .emailNotificationsEnabled(false)
                .smsNotificationsEnabled(true)
                .pushNotificationsEnabled(true)
                .categoryPreferences(categoryPreferences)
                .quietHoursStart(22)
                .quietHoursEnd(6)
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .deviceToken("device-token-123")
                .build();

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPreferences));
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationPreferencesResponse response = preferencesService.updatePreferences(userId, request);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        assertThat(savedPreferences.isAppNotificationsEnabled()).isTrue();
        assertThat(savedPreferences.isEmailNotificationsEnabled()).isFalse();
        assertThat(savedPreferences.isSmsNotificationsEnabled()).isTrue();
        assertThat(savedPreferences.isPushNotificationsEnabled()).isTrue();
        assertThat(savedPreferences.getCategoryPreferences()).containsEntry("PAYMENT_REQUEST", false);
        assertThat(savedPreferences.getQuietHoursStart()).isEqualTo(22);
        assertThat(savedPreferences.getQuietHoursEnd()).isEqualTo(6);
        assertThat(savedPreferences.getEmail()).isEqualTo("test@example.com");
        assertThat(savedPreferences.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(savedPreferences.getDeviceToken()).isEqualTo("device-token-123");

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.isAppNotificationsEnabled()).isTrue();
        assertThat(response.isEmailNotificationsEnabled()).isFalse();
        assertThat(response.isSmsNotificationsEnabled()).isTrue();
        assertThat(response.isPushNotificationsEnabled()).isTrue();
    }

    @Test
    void updatePreferences_ShouldOnlyUpdateProvidedFields() {
        // Given
        NotificationPreferences existingPreferences = NotificationPreferences.createDefault(userId);
        existingPreferences.setEmailNotificationsEnabled(true);
        existingPreferences.setSmsNotificationsEnabled(false);
        existingPreferences.updateContactInfo("old@example.com", "+9876543210", "old-token");

        // Only updating email notifications and email address
        UpdatePreferencesRequest request = UpdatePreferencesRequest.builder()
                .emailNotificationsEnabled(false)
                .email("new@example.com")
                .build();

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPreferences));
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        preferencesService.updatePreferences(userId, request);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        // These should be updated
        assertThat(savedPreferences.isEmailNotificationsEnabled()).isFalse();
        assertThat(savedPreferences.getEmail()).isEqualTo("new@example.com");

        // These should remain unchanged
        assertThat(savedPreferences.isAppNotificationsEnabled()).isEqualTo(existingPreferences.isAppNotificationsEnabled());
        assertThat(savedPreferences.isSmsNotificationsEnabled()).isEqualTo(existingPreferences.isSmsNotificationsEnabled());
        assertThat(savedPreferences.isPushNotificationsEnabled()).isEqualTo(existingPreferences.isPushNotificationsEnabled());
        assertThat(savedPreferences.getPhoneNumber()).isEqualTo(existingPreferences.getPhoneNumber());
        assertThat(savedPreferences.getDeviceToken()).isEqualTo(existingPreferences.getDeviceToken());
    }

    @Test
    void getPreferences_ShouldReturnPreferencesResponse() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", "+1234567890", "device-token");

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When
        NotificationPreferencesResponse response = preferencesService.getPreferences(userId);

        // Then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.isAppNotificationsEnabled()).isEqualTo(preferences.isAppNotificationsEnabled());
        assertThat(response.isEmailNotificationsEnabled()).isEqualTo(preferences.isEmailNotificationsEnabled());
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(response.isDeviceTokenRegistered()).isTrue();
    }

    @Test
    void isNotificationEnabled_ShouldRespectQuietHours() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setQuietHours(22, 6);
        preferences.setAppNotificationsEnabled(true);
        preferences.setEmailNotificationsEnabled(true);
        preferences.setSmsNotificationsEnabled(true);
        preferences.setPushNotificationsEnabled(true);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // Mock that it's quiet hours
        NotificationPreferences spyPreferences = spy(preferences);
        when(spyPreferences.isQuietHours()).thenReturn(true);
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(spyPreferences));

        // When & Then
        // During quiet hours, only APP notifications should be enabled
        assertThat(preferencesService.isNotificationEnabled(userId, "PAYMENT_REQUEST", NotificationType.APP))
                .isTrue();
        assertThat(preferencesService.isNotificationEnabled(userId, "PAYMENT_REQUEST", NotificationType.EMAIL))
                .isFalse();
        assertThat(preferencesService.isNotificationEnabled(userId, "PAYMENT_REQUEST", NotificationType.SMS))
                .isFalse();
        assertThat(preferencesService.isNotificationEnabled(userId, "PAYMENT_REQUEST", NotificationType.PUSH))
                .isFalse();
    }

    @Test
    void isNotificationEnabled_ShouldRespectCategoryPreferences() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setAppNotificationsEnabled(true);
        preferences.setCategoryPreference("PAYMENT_REQUEST", false);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));

        // When & Then
        // Even though APP notifications are enabled, the category is disabled
        assertThat(preferencesService.isNotificationEnabled(userId, "PAYMENT_REQUEST", NotificationType.APP))
                .isFalse();
    }

    @Test
    void updateDeviceToken_ShouldUpdateTokenAndEnablePushNotifications() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.setPushNotificationsEnabled(false);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String newToken = "new-device-token";

        // When
        preferencesService.updateDeviceToken(userId, newToken);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        assertThat(savedPreferences.getDeviceToken()).isEqualTo(newToken);
        assertThat(savedPreferences.isPushNotificationsEnabled()).isTrue();
    }

    @Test
    void updateEmail_ShouldUpdateEmailAddress() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String newEmail = "new@example.com";

        // When
        preferencesService.updateEmail(userId, newEmail);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        assertThat(savedPreferences.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void updatePhoneNumber_ShouldUpdatePhoneNumber() {
        // Given
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(preferences));
        when(preferencesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String newPhone = "+9876543210";

        // When
        preferencesService.updatePhoneNumber(userId, newPhone);

        // Then
        verify(preferencesRepository).save(preferencesCaptor.capture());
        NotificationPreferences savedPreferences = preferencesCaptor.getValue();

        assertThat(savedPreferences.getPhoneNumber()).isEqualTo(newPhone);
    }
}