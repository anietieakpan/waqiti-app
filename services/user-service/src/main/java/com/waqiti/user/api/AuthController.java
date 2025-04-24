package com.waqiti.user.api;

import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.dto.TokenRefreshRequest;
import com.waqiti.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Authentication request received for user: {}", request.getUsernameOrEmail());
        return ResponseEntity.ok(authService.authenticate(request));
    }


    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthenticationResponse> verifyMfa(
            @RequestHeader(value = "X-MFA-Token", required = false) String xMfaToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MfaVerifyRequest request) {

        // Extract token from Authorization header if X-MFA-Token is not present
        String mfaToken = xMfaToken;
        if (mfaToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            mfaToken = authHeader.substring(7);
        }

        if (mfaToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthenticationResponse response = authService.verifyMfa(mfaToken, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request received");
        // Strip "Bearer " prefix if present
        String refreshToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

}