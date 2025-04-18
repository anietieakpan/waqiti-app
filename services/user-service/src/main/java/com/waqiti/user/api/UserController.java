// File: src/main/java/com/waqiti/user/api/UserController.java
package com.waqiti.user.api;

import com.waqiti.user.domain.VerificationType;
import com.waqiti.user.dto.*;
import com.waqiti.user.dto.*;
import com.waqiti.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration request received for: {}", request.getUsername());
        return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        log.info("Email verification request received");
        boolean verified = userService.verifyToken(token, VerificationType.EMAIL);
        return ResponseEntity.ok(verified ? "Account verified successfully" : "Verification failed");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Current user request received");
        // Get authentication from security context instead of relying on @AuthenticationPrincipal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        log.debug("Getting user details for authenticated user ID: {}", userId);
        UserResponse user = userService.getUserById(UUID.fromString(userId));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("User request received for ID: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Profile update request received");
        // Get the user ID from the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PostMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request received");
        // Get the user ID from the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = UUID.fromString(authentication.getName());
        boolean changed = userService.changePassword(userId, request);
        return ResponseEntity.ok(changed ? "Password changed successfully" : "Password change failed");
    }

    @PostMapping("/password/reset/request")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetInitiationRequest request) {
        log.info("Password reset request received for email: {}", request.getEmail());
        boolean initiated = userService.initiatePasswordReset(request);
        return ResponseEntity.ok(initiated ?
                "Password reset instructions sent to your email" :
                "Failed to initiate password reset");
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset with token request received");
        boolean reset = userService.resetPassword(request);
        return ResponseEntity.ok(reset ? "Password reset successfully" : "Password reset failed");
    }
}