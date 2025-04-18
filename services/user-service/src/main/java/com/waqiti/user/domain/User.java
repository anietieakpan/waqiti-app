package com.waqiti.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^\\+[0-9]{10,15}$");

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "kyc_status")
    private KycStatus kycStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Audit fields
    @Setter
    @Column(name = "created_by")
    private String createdBy;
    
    @Setter
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Creates a new user
     */
    public static User create(String username, String email, String passwordHash, String externalId) {
        validateUsername(username);
        validateEmail(email);
        
        User user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.externalId = externalId;
        user.status = UserStatus.PENDING;
        user.kycStatus = KycStatus.NOT_STARTED;
        user.roles.add("ROLE_USER");
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    /**
     * Activates the user account
     */
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspends the user account
     */
    public void suspend(String reason) {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Closes the user account permanently
     */
    public void close() {
        this.status = UserStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the user's KYC status
     */
    public void updateKycStatus(KycStatus newStatus) {
        this.kycStatus = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a role to the user
     */
    public void addRole(String role) {
        this.roles.add(role);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the user's password
     */
    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the user's phone number
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            validatePhoneNumber(phoneNumber);
        }
        
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates if the user's status is active
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * Validates the username format
     */
    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, periods, underscores, and hyphens");
        }
    }

    /**
     * Validates the email format
     */
    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validates the phone number format
     */
    private static void validatePhoneNumber(String phoneNumber) {
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number format. Must start with + followed by 10-15 digits");
        }
    }
}