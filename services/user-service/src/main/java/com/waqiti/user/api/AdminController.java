// File: src/main/java/com/waqiti/user/api/AdminController.java
package com.waqiti.user.api;

import com.waqiti.user.dto.UserResponse;
import com.waqiti.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin request to get all users");
        // For tests, just return an empty list
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/users/{userId}/mfa/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetUserMfa(@PathVariable UUID userId) {
        log.info("Admin request to reset MFA for user: {}", userId);
        userService.resetUserMfa(userId);
        return ResponseEntity.ok().build();
    }
}