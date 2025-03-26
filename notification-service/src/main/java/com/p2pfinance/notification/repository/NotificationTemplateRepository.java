package com.p2pfinance.notification.repository;

import com.p2pfinance.notification.domain.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    /**
     * Find template by code
     */
    Optional<NotificationTemplate> findByCode(String code);

    /**
     * Find templates by category
     */
    List<NotificationTemplate> findByCategory(String category);

    /**
     * Find enabled templates
     */
    List<NotificationTemplate> findByEnabledTrue();

    /**
     * Find enabled templates by category
     */
    List<NotificationTemplate> findByCategoryAndEnabledTrue(String category);

    /**
     * Check if a template exists by code
     */
    boolean existsByCode(String code);
}