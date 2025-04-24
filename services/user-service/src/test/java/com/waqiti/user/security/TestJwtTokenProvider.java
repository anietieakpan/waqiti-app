// File: src/test/java/com/waqiti/user/security/TestJwtTokenProvider.java
package com.waqiti.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.*;

@Slf4j
public class TestJwtTokenProvider extends JwtTokenProvider {

    private static final String TEST_KEY = "VGhpc0lzQVZlcnlMb25nQW5kU2VjdXJlVGVzdEtleVRoYXRJc1N1ZmZpY2llbnRseUxvbmdGb3JUaGVITUFDU0hBQWxnb3JpdGhtMTIzNDU2Nzg5";
    private Key key;

    // Override the parent's init method to prevent it from being called
    @Override
    @PostConstruct
    protected void init() {
        // Do nothing - we'll use our own initialization in initTestProvider
    }

    @PostConstruct
    public void initTestProvider() {
        log.debug("Initializing test JWT token provider");
        byte[] keyBytes = Base64.getDecoder().decode(TEST_KEY);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Override methods to always validate in tests
    @Override
    public boolean validateToken(String token) {
        log.debug("Test JWT provider always validates tokens as true");
        return true;
    }

    @Override
    public Claims getClaimsFromToken(String token) {
        try {
            // Try to parse real claims if possible
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            // For tests, if parsing fails, create dummy claims
            log.debug("Creating mock claims for test");
            Claims claims = Jwts.claims();
            claims.setSubject("testuser");
            claims.put("userId", UUID.randomUUID().toString());
            claims.put("mfa_required", true);
            return claims;
        }
    }

    @Override
    public String getUsername(String token) {
        try {
            return getClaimsFromToken(token).getSubject();
        } catch (Exception e) {
            return "testuser";
        }
    }

    @Override
    public UUID getUserId(String token) {
        try {
            String userId = (String) getClaimsFromToken(token).get("userId");
            return UUID.fromString(userId);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }

    @Override
    public String createToken(UUID userId, String username,
                              Collection<? extends GrantedAuthority> authorities,
                              Map<String, Object> additionalClaims,
                              long validityInMilliseconds) {
        // Create an actual token for tests
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId.toString());
        claims.put("auth", authorities);

        if (additionalClaims != null) {
            additionalClaims.forEach(claims::put);
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String createAccessToken(UUID userId, String username,
                                    Collection<? extends GrantedAuthority> authorities) {
        return createToken(userId, username, authorities, new HashMap<>(), 3600000);
    }

    @Override
    public String createRefreshToken(UUID userId, String username) {
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("tokenType", "refresh");
        return createToken(userId, username, Collections.emptyList(), additionalClaims, 86400000);
    }
}