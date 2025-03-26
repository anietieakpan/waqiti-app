package com.p2pfinance.user.repository;

import com.p2pfinance.user.domain.VerificationToken;
import com.p2pfinance.user.domain.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    /**
     * Find a token by its value
     */
    Optional<VerificationToken> findByToken(String token);
    
    /**
     * Find the most recent token for a user and verification type
     */
    Optional<VerificationToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, VerificationType type);
    
    /**
     * Find all tokens for a user
     */
    List<VerificationToken> findByUserId(UUID userId);
    
    /**
     * Find all tokens for a user and verification type
     */
    List<VerificationToken> findByUserIdAndType(UUID userId, VerificationType type);
    
    /**
     * Find expired tokens that have not been used
     */
    List<VerificationToken> findByUsedFalseAndExpiryDateBefore(LocalDateTime date);
}