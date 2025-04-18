// File: services/user-service/src/main/java/com/waqiti/user/domain/MfaConfiguration.java
package com.waqiti.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mfa_configurations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MfaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaMethod method;

    @Column(nullable = false)
    private boolean enabled;

    @Column
    private String secret;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Creates a new MFA configuration
     */
    public static MfaConfiguration create(UUID userId, MfaMethod method, String secret) {
        MfaConfiguration config = new MfaConfiguration();
        config.userId = userId;
        config.method = method;
        config.enabled = false;
        config.secret = secret;
        config.verified = false;
        config.createdAt = LocalDateTime.now();
        config.updatedAt = LocalDateTime.now();
        return config;
    }

    /**
     * Enables this MFA method after verification
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disables this MFA method
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this MFA method as verified
     */
    public void markVerified() {
        this.verified = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the secret key for this MFA method
     */
    public void updateSecret(String secret) {
        this.secret = secret;
        this.updatedAt = LocalDateTime.now();
    }
}