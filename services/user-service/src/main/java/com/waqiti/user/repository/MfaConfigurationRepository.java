// File: services/user-service/src/main/java/com/waqiti/user/repository/MfaConfigurationRepository.java
package com.waqiti.user.repository;

import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaConfigurationRepository extends JpaRepository<MfaConfiguration, UUID> {

    /**
     * Find all MFA configurations for a user
     */
    List<MfaConfiguration> findByUserId(UUID userId);

    /**
     * Find a specific MFA configuration for a user by method
     */
    Optional<MfaConfiguration> findByUserIdAndMethod(UUID userId, MfaMethod method);

    /**
     * Find enabled MFA configurations for a user
     */
    List<MfaConfiguration> findByUserIdAndEnabledTrue(UUID userId);

    /**
     * Find a verified and enabled MFA configuration
     */
    Optional<MfaConfiguration> findByUserIdAndMethodAndEnabledTrueAndVerifiedTrue(UUID userId, MfaMethod method);

    /**
     * Check if a user has any enabled MFA methods
     */
    boolean existsByUserIdAndEnabledTrueAndVerifiedTrue(UUID userId);
}