package com.waqiti.notification.repository;

import com.waqiti.notification.domain.DeliveryStatus;
import com.waqiti.notification.domain.Notification;
import com.waqiti.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    /**
     * Find notifications by user ID, sorted by creation date
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find unread notifications by user ID
     */
    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find notifications by user ID and category
     */
    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(UUID userId, String category, Pageable pageable);

    /**
     * Find notifications by delivery status
     */
    List<Notification> findByDeliveryStatus(DeliveryStatus status);

    /**
     * Find notifications by type and delivery status
     */
    List<Notification> findByTypeAndDeliveryStatus(NotificationType type, DeliveryStatus status);

    /**
     * Count unread notifications by user ID
     */
    long countByUserIdAndReadFalse(UUID userId);

    /**
     * Find expired unread notifications to clean up
     */
    List<Notification> findByReadFalseAndExpiresAtBeforeAndDeliveryStatus(
            LocalDateTime now, DeliveryStatus status);

    /**
     * Find notifications by reference ID
     */
    List<Notification> findByReferenceId(String referenceId);

    /**
     * Find latest notification by user ID and category
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.category = :category " +
            "ORDER BY n.createdAt DESC LIMIT 1")
    Notification findLatestByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") String category);
}