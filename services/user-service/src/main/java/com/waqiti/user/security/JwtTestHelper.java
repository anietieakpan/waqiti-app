// File: src/test/java/com/waqiti/user/security/JwtTestHelper.java
package com.waqiti.user.security;

import com.waqiti.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for JWT-related test utilities
 */
public class JwtTestHelper {

    /**
     * Sets up authentication in the security context for tests
     */
    public static void setupAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null, // No credentials needed for this test auth
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}