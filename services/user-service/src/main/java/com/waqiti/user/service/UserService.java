package com.waqiti.user.service;

import com.waqiti.user.client.IntegrationServiceClient;
import com.waqiti.user.client.dto.CreateUserRequest;
import com.waqiti.user.client.dto.CreateUserResponse;
import com.waqiti.user.client.dto.UpdateUserRequest;
import com.waqiti.user.domain.*;
import com.waqiti.user.dto.*;
import com.waqiti.user.domain.*;
import com.waqiti.user.dto.*;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.repository.VerificationTokenRepository;
import com.waqiti.user.security.JwtTokenProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final IntegrationServiceClient integrationClient;

    /**
     * Register a new user
     */
    @Transactional
    @CircuitBreaker(name = "integrationService", fallbackMethod = "registerUserFallback")
    @Retry(name = "integrationService")
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate user doesn't already exist
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists: " + request.getPhoneNumber());
        }

        // Create user in the external system
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Generate user ID first for external system creation
        UUID userId = UUID.randomUUID();

        // Create user in external system
        CreateUserResponse externalUserResponse = integrationClient.createUser(
                CreateUserRequest.builder()
                        .userId(userId)
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .externalSystem("FINERACT") // Using Fineract by default
                        .build()
        );

        // Create user in our system
        User user = User.create(
                request.getUsername(),
                request.getEmail(),
                hashedPassword,
                externalUserResponse.getExternalId()
        );

        user.setId(userId);
        user.updatePhoneNumber(request.getPhoneNumber());
        user = userRepository.save(user);

        // Create user profile
        UserProfile profile = UserProfile.create(user);
        profileRepository.save(profile);

        // Generate verification token
        generateVerificationToken(user.getId(), VerificationType.EMAIL);

        return mapToUserResponse(user, profile);
    }

    /**
     * Fallback method for user registration when integration service is unavailable
     */
    private UserResponse registerUserFallback(UserRegistrationRequest request, Throwable t) {
        log.warn("Fallback for registerUser executed due to: {}", t.getMessage());
        throw new RuntimeException("Unable to register user at this time. Please try again later.");
    }

    /**
     * Authenticate a user and generate JWT tokens
     */
    @Transactional
    public AuthenticationResponse authenticateUser(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details for token generation
        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // Get our user entity
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found after authentication: " + userDetails.getUsername()));

        // If the user is not active, reject authentication
        if (!user.isActive()) {
            throw new InvalidUserStateException("User account is not active");
        }

        // Generate tokens
        String accessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getUsername(),
                userDetails.getAuthorities()
        );

        String refreshToken = tokenProvider.createRefreshToken(
                user.getId(),
                user.getUsername()
        );

        UserProfile profile = profileRepository.findById(user.getId()).orElse(null);

        // Build response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .user(mapToUserResponse(user, profile))
                .build();
    }

    /**
     * Generate a verification token
     */
    @Transactional
    public String generateVerificationToken(UUID userId, VerificationType type) {
        log.info("Generating verification token for user: {}, type: {}", userId, type);

        // Ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Generate a random token
        String tokenValue = UUID.randomUUID().toString();

        // Save the token
        VerificationToken token = VerificationToken.create(
                userId,
                tokenValue,
                type,
                30 * 24 * 60 // 30 days in minutes
        );

        tokenRepository.save(token);

        return tokenValue;
    }

    /**
     * Verify a token and perform the associated action
     */
    @Transactional
    public boolean verifyToken(String token, VerificationType type) {
        log.info("Verifying token for type: {}", type);

        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid token"));

        if (!verificationToken.getType().equals(type)) {
            throw new InvalidVerificationTokenException("Token is not of the required type");
        }

        if (!verificationToken.isValid()) {
            throw new InvalidVerificationTokenException("Token is expired or already used");
        }

        // Mark token as used
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        // Perform action based on token type
        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(verificationToken.getUserId()));

        switch (type) {
            case EMAIL:
                user.activate();
                userRepository.save(user);
                break;
            // Handle other verification types
            default:
                log.warn("Unhandled verification type: {}", type);
                return false;
        }

        return true;
    }

    /**
     * Get a user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserProfile profile = profileRepository.findById(userId).orElse(null);

        return mapToUserResponse(user, profile);
    }

    /**
     * Update a user's profile
     */
    @Transactional
    @CircuitBreaker(name = "integrationService", fallbackMethod = "updateProfileFallback")
    @Retry(name = "integrationService")
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> UserProfile.create(user));

        // Update profile data
        profile.updateName(request.getFirstName(), request.getLastName());

        if (request.getDateOfBirth() != null) {
            profile.updateDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddressLine1() != null) {
            profile.updateAddress(
                    request.getAddressLine1(),
                    request.getAddressLine2(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry()
            );
        }

        if (request.getPreferredLanguage() != null || request.getPreferredCurrency() != null) {
            profile.updatePreferences(
                    request.getPreferredLanguage(),
                    request.getPreferredCurrency()
            );
        }

        // Save profile
        profile = profileRepository.save(profile);

        // Update in external system if necessary
        integrationClient.updateUser(
                UpdateUserRequest.builder()
                        .externalId(user.getExternalId())
                        .externalSystem("FINERACT") // Using Fineract by default
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .build()
        );

        return mapToUserResponse(user, profile);
    }

    /**
     * Fallback method for profile updates when integration service is unavailable
     */
    private UserResponse updateProfileFallback(UUID userId, UpdateProfileRequest request, Throwable t) {
        log.warn("Fallback for updateProfile executed due to: {}", t.getMessage());

        // We can still update our local database even if the external system is down
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> UserProfile.create(user));

        // Update profile data
        profile.updateName(request.getFirstName(), request.getLastName());

        if (request.getDateOfBirth() != null) {
            profile.updateDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddressLine1() != null) {
            profile.updateAddress(
                    request.getAddressLine1(),
                    request.getAddressLine2(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry()
            );
        }

        if (request.getPreferredLanguage() != null || request.getPreferredCurrency() != null) {
            profile.updatePreferences(
                    request.getPreferredLanguage(),
                    request.getPreferredCurrency()
            );
        }

        // Save profile
        profile = profileRepository.save(profile);

        return mapToUserResponse(user, profile);
    }

    /**
     * Change user password
     */
    @Transactional
    public boolean changePassword(UUID userId, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Current password is incorrect");
        }

        // Update password
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    /**
     * Initiate password reset
     */
    @Transactional
    public boolean initiatePasswordReset(PasswordResetInitiationRequest request) {
        log.info("Initiating password reset for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        // Generate password reset token
        String token = generateVerificationToken(user.getId(), VerificationType.PASSWORD_RESET);

        // In a real implementation, we would send an email with the reset link
        // For now, just log it
        log.info("Password reset token generated: {} for user: {}", token, user.getId());

        return true;
    }

    /**
     * Reset password using token
     */
    @Transactional
    public boolean resetPassword(PasswordResetRequest request) {
        log.info("Resetting password with token");

        VerificationToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid token"));

        if (!token.getType().equals(VerificationType.PASSWORD_RESET)) {
            throw new InvalidVerificationTokenException("Token is not a password reset token");
        }

        if (!token.isValid()) {
            throw new InvalidVerificationTokenException("Token is expired or already used");
        }

        // Mark token as used
        token.markAsUsed();
        tokenRepository.save(token);

        // Update password
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException(token.getUserId()));

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    /**
     * Map a User entity to a UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user, UserProfile profile) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus().toString())
                .kycStatus(user.getKycStatus().toString())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt());

        if (profile != null) {
            builder.profile(UserProfileResponse.builder()
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .dateOfBirth(profile.getDateOfBirth())
                    .addressLine1(profile.getAddressLine1())
                    .addressLine2(profile.getAddressLine2())
                    .city(profile.getCity())
                    .state(profile.getState())
                    .postalCode(profile.getPostalCode())
                    .country(profile.getCountry())
                    .profilePictureUrl(profile.getProfilePictureUrl())
                    .preferredLanguage(profile.getPreferredLanguage())
                    .preferredCurrency(profile.getPreferredCurrency())
                    .build());
        }

        return builder.build();
    }
}