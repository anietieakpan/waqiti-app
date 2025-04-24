// File: src/test/java/com/waqiti/user/security/SecurityContextHelper.java
package com.waqiti.user.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Profile("test")
public class SecurityContextHelper extends OncePerRequestFilter {

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // Set the authentication name to a valid UUID for MFA controller
            request.setAttribute("TEST_USER_ID", TEST_USER_ID.toString());
        }

        filterChain.doFilter(request, response);
    }

    // Add this helper method for test classes to use
    public static UUID getTestUserId() {
        return TEST_USER_ID;
    }
}