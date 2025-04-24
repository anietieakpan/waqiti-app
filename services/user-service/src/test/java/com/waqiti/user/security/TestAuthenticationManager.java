// File: src/test/java/com/waqiti/user/security/TestAuthenticationManager.java
package com.waqiti.user.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
@Primary
@Slf4j
public class TestAuthenticationManager implements AuthenticationManager {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.debug("Test authentication manager always authenticates successfully for principal: {}", authentication.getPrincipal());

        // Create a proper UserDetails object (not just a String)
        UserDetails userDetails = User.builder()
                .username(authentication.getName())
                .password("") // We're not validating passwords in tests
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Create an authenticated token with UserDetails as the principal
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,  // principal (was previously just username string)
                authentication.getCredentials(), // password
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        log.debug("Successfully created authenticated token for test user: {}", userDetails.getUsername());
        return authToken;
    }
}