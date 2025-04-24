// File: src/test/java/com/waqiti/user/auth/TestAuthController.java
package com.waqiti.user.auth;

import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/test-api/auth")
@RequiredArgsConstructor
@Slf4j
@Profile("test")
public class TestAuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> testLogin(@RequestBody AuthenticationRequest request) {
        log.info("Test login for user: {}", request.getUsernameOrEmail());

        // For basic auth tests, return a complete auth response with access token
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken("test-access-token-" + UUID.randomUUID())
                .refreshToken("test-refresh-token-" + UUID.randomUUID())
                .tokenType("Bearer")
                .expiresIn(3600L)
                .requiresMfa(false)
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username(request.getUsernameOrEmail())
                        .email(request.getUsernameOrEmail() + "@example.com")
                        .build())
                .build());
    }

    @PostMapping("/mfa-login")
    public ResponseEntity<AuthenticationResponse> testMfaLogin(@RequestBody AuthenticationRequest request) {
        log.info("Test MFA-required login for user: {}", request.getUsernameOrEmail());

        // For MFA tests, return a response that requires MFA
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .mfaToken("test-mfa-token-" + UUID.randomUUID())
                .requiresMfa(true)
                .availableMfaMethods(Arrays.asList(MfaMethod.TOTP, MfaMethod.SMS))
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username(request.getUsernameOrEmail())
                        .email(request.getUsernameOrEmail() + "@example.com")
                        .build())
                .build());
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthenticationResponse> testVerifyMfa(
            @RequestBody MfaVerifyRequest request,
            @RequestHeader(value = "X-MFA-Token", required = false) String xMfaToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("Test MFA verification with method: {}", request.getMethod());

        // For MFA verification tests, return a complete auth response
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken("test-access-token-after-mfa-" + UUID.randomUUID())
                .refreshToken("test-refresh-token-after-mfa-" + UUID.randomUUID())
                .tokenType("Bearer")
                .expiresIn(3600L)
                .requiresMfa(false)
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username("test-user-after-mfa")
                        .email("test-user-after-mfa@example.com")
                        .build())
                .build());
    }

    @PostMapping("/setup/totp")
    public ResponseEntity<Object> testSetupTotp() {
        log.info("Test TOTP setup");

        // Return a mock TOTP setup response
        return ResponseEntity.ok(java.util.Map.of(
                "secret", "TESTSECRETKEY",
                "qrCodeImage", "data:image/png;base64,MockQRCodeBase64Data"
        ));
    }

    @PostMapping("/verify/totp")
    public ResponseEntity<Void> testVerifyTotp(@RequestParam String code) {
        log.info("Test verify TOTP with code: {}", code);

        // Always succeed
        return ResponseEntity.ok().build();
    }

    @PostMapping("/setup/sms")
    public ResponseEntity<Void> testSetupSms(@RequestParam String phoneNumber) {
        log.info("Test SMS setup with phone: {}", phoneNumber);

        // Always succeed
        return ResponseEntity.ok().build();
    }
}