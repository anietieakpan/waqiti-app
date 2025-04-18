// File: services/user-service/src/main/java/com/waqiti/user/api/MfaController.java
package com.waqiti.user.api;

import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.dto.*;
import com.waqiti.user.service.MfaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
@Slf4j
public class MfaController {

    private final MfaService mfaService;

    /**
     * Get MFA status for the current user
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaStatusResponse> getMfaStatus() {
        UUID userId = getCurrentUserId();
        log.info("Getting MFA status for user: {}", userId);

        boolean enabled = mfaService.isMfaEnabled(userId);
        List<MfaMethod> methods = mfaService.getEnabledMfaMethods(userId);

        MfaStatusResponse response = MfaStatusResponse.builder()
                .enabled(enabled)
                .methods(methods)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Set up TOTP (authenticator app) MFA
     */
    @PostMapping("/setup/totp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MfaSetupResponse> setupTotp() {
        UUID userId = getCurrentUserId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Setting up TOTP for user: {}", userId);

        MfaSetupResponse response = mfaService.setupTotp(userId, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify TOTP setup
     */
    @PostMapping("/verify/totp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verifyTotpSetup(@RequestParam String code) {
        UUID userId = getCurrentUserId();
        log.info("Verifying TOTP setup for user: {}", userId);

        boolean verified = mfaService.verifyTotpSetup(userId, code);

        if (verified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set up SMS MFA
     */
    @PostMapping("/setup/sms")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> setupSms(@RequestParam String phoneNumber) {
        UUID userId = getCurrentUserId();
        log.info("Setting up SMS MFA for user: {}", userId);

        boolean success = mfaService.setupSms(userId, phoneNumber);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set up Email MFA
     */
    @PostMapping("/setup/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> setupEmail(@RequestParam String email) {
        UUID userId = getCurrentUserId();
        log.info("Setting up Email MFA for user: {}", userId);

        boolean success = mfaService.setupEmail(userId, email);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify SMS or Email setup
     */
    @PostMapping("/verify/{method}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> verifyCodeSetup(
            @PathVariable MfaMethod method,
            @RequestParam String code) {

        UUID userId = getCurrentUserId();
        log.info("Verifying {} setup for user: {}", method, userId);

        boolean verified = mfaService.verifyCodeSetup(userId, method, code);

        if (verified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resend verification code
     */
    @PostMapping("/resend/{method}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resendVerificationCode(@PathVariable MfaMethod method) {
        UUID userId = getCurrentUserId();
        log.info("Resending verification code for method: {} and user: {}", method, userId);

        boolean success = mfaService.resendVerificationCode(userId, method);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Disable MFA method
     */
    @PostMapping("/disable/{method}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disableMfaMethod(@PathVariable MfaMethod method) {
        UUID userId = getCurrentUserId();
        log.info("Disabling MFA method: {} for user: {}", method, userId);

        boolean success = mfaService.disableMfaMethod(userId, method);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to get the current authenticated user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }

        return UUID.fromString(authentication.getName());
    }
}