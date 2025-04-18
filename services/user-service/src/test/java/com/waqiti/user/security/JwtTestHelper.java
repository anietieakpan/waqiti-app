// File: src/test/java/com/waqiti/user/security/JwtTestHelper.java
package com.waqiti.user.security;

import com.waqiti.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

/**
 * Helper class for JWT authentication in tests
 */
public class JwtTestHelper {

    /**
     * Set up authentication in SecurityContext for test user
     * @param user The user to set up authentication for
     */
    public static void setupAuthentication(User user) {
        // Create authentication based on user roles
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );

        // Set in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}