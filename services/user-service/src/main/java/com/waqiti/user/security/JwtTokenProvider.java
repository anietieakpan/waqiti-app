package com.waqiti.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.access-token-expire-length:3600000}")
    private long accessTokenValidityInMilliseconds; // 1h by default

    @Value("${security.jwt.token.refresh-token-expire-length:2592000000}")
    private long refreshTokenValidityInMilliseconds; // 30 days by default

    private Key key;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Set<String> revokedTokens = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(UUID userId, String username,
                              Collection<? extends GrantedAuthority> authorities,
                              Map<String, Object> additionalClaims,
                              long validityInMilliseconds) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId.toString());
        claims.put("auth", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Add a token identifier to support revocation
        String tokenId = generateTokenId();
        claims.setId(tokenId);

        // Add additional claims
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

    public String createAccessToken(UUID userId, String username,
                                    Collection<? extends GrantedAuthority> authorities,
                                    Map<String, Object> additionalClaims) {
        return createToken(userId, username, authorities, additionalClaims, accessTokenValidityInMilliseconds);
    }


    public String createAccessToken(UUID userId, String username,
                                    Collection<? extends GrantedAuthority> authorities) {
        return createAccessToken(userId, username, authorities, new HashMap<>());
    }

    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInMilliseconds / 1000;
    }


    public String createRefreshToken(UUID userId, String username) {
        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("tokenType", "refresh");
        return createToken(userId, username, Collections.emptyList(), additionalClaims, refreshTokenValidityInMilliseconds);
    }

    public boolean validateToken(String token) {
        try {
            // Check if token has been revoked
            if (isTokenRevoked(token)) {
                return false;
            }

            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public UUID getUserId(String token) {
        String userId = (String) getClaimsFromToken(token).get("userId");
        return UUID.fromString(userId);
    }

    public void revokeToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenId = claims.getId();
            revokedTokens.add(tokenId);
            log.info("Token revoked: {}", tokenId);
        } catch (Exception e) {
            log.error("Error revoking token", e);
        }
    }

    private boolean isTokenRevoked(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenId = claims.getId();
            return revokedTokens.contains(tokenId);
        } catch (Exception e) {
            return true;
        }
    }

    private String generateTokenId() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
}