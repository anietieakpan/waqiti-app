/**
 * File: src/test/java/com/p2pfinance/notification/api/NotificationControllerTest.java
 */
package com.p2pfinance.notification.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.notification.dto.NotificationListResponse;
import com.p2pfinance.notification.dto.NotificationResponse;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.repository.NotificationPreferencesRepository;
import com.p2pfinance.notification.repository.NotificationRepository;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import com.p2pfinance.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.config.import=optional:configserver:",
        "eureka.client.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@ActiveProfiles("test")
@Tag("UnitTest")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    // Mock all repositories to prevent JPA initialization
    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationPreferencesRepository preferencesRepository;

    @MockBean
    private NotificationTemplateRepository templateRepository;

    private UUID userId;
    private UUID notificationId;
    private NotificationResponse sampleNotification;
    private List<NotificationResponse> sampleNotifications;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        // Create sample notification response
        sampleNotification = NotificationResponse.builder()
                .id(notificationId)
                .userId(userId)
                .title("Test Notification")
                .message("This is a test notification")
                .type("APP")
                .category("TEST")
                .referenceId("ref-123")
                .read(false)
                .actionUrl("/test/action")
                .createdAt(LocalDateTime.now())
                .deliveryStatus("SENT")
                .build();

        // Create list of sample notifications
        sampleNotifications = Arrays.asList(
                sampleNotification,
                NotificationResponse.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .title("Another Notification")
                        .message("This is another test notification")
                        .type("APP")
                        .category("TEST")
                        .read(true)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .readAt(LocalDateTime.now().minusHours(1))
                        .deliveryStatus("SENT")
                        .build()
        );
    }

    @Test
    void sendNotification_ShouldReturnCreatedNotifications() throws Exception {
        // Given
        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .templateCode("test_template")
                .parameters(Map.of("key", "value"))
                .build();

        when(notificationService.sendNotification(any(SendNotificationRequest.class)))
                .thenReturn(sampleNotifications);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Notification"))
                .andExpect(jsonPath("$[0].message").value("This is a test notification"))
                .andExpect(jsonPath("$[0].type").value("APP"))
                .andExpect(jsonPath("$[0].read").value(false));

        verify(notificationService).sendNotification(any(SendNotificationRequest.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getNotifications_ShouldReturnUserNotifications() throws Exception {
        // Given
        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(sampleNotifications)
                .unreadCount(1)
                .totalPages(1)
                .totalElements(2)
                .page(0)
                .size(10)
                .build();

        when(notificationService.getNotifications(any(UUID.class), any(Pageable.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(2)))
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(notificationService).getNotifications(any(UUID.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getUnreadNotifications_ShouldReturnUnreadNotifications() throws Exception {
        // Given
        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(List.of(sampleNotification))
                .unreadCount(1)
                .totalPages(1)
                .totalElements(1)
                .page(0)
                .size(10)
                .build();

        when(notificationService.getUnreadNotifications(any(UUID.class), any(Pageable.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/unread")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.notifications[0].read").value(false));

        verify(notificationService).getUnreadNotifications(any(UUID.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void getNotification_ShouldReturnNotificationById() throws Exception {
        // Given
        when(notificationService.getNotification(notificationId))
                .thenReturn(sampleNotification);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.title").value("Test Notification"))
                .andExpect(jsonPath("$.message").value("This is a test notification"));

        verify(notificationService).getNotification(notificationId);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void markAsRead_ShouldMarkNotificationAsRead() throws Exception {
        // Given
        NotificationResponse updatedNotification = NotificationResponse.builder()
                .id(notificationId)
                .userId(userId)
                .title("Test Notification")
                .message("This is a test notification")
                .type("APP")
                .category("TEST")
                .referenceId("ref-123")
                .read(true)
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusHours(1))
                .deliveryStatus("SENT")
                .build();

        when(notificationService.markAsRead(notificationId))
                .thenReturn(updatedNotification);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        verify(notificationService).markAsRead(notificationId);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void markAllAsRead_ShouldMarkAllNotificationsAsRead() throws Exception {
        // Given - no special setup needed

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/read-all"))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(any(UUID.class));
    }

    @Test
    void getNotifications_ShouldRequireAuthentication() throws Exception {
        // When & Then - No authentication provided
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());

        verify(notificationService, never()).getNotifications(any(), any());
    }

    @Test
    void sendNotification_ShouldValidateRequest() throws Exception {
        // Given
        SendNotificationRequest invalidRequest = SendNotificationRequest.builder()
                // Missing required fields
                .parameters(Map.of("key", "value"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendNotification(any());
    }
}