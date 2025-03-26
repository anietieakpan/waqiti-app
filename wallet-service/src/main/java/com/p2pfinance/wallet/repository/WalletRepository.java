package com.p2pfinance.wallet.repository;

import com.p2pfinance.wallet.domain.Wallet;
import com.p2pfinance.wallet.domain.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Find all wallets for a user
     */
    List<Wallet> findByUserId(UUID userId);

    /**
     * Find all active wallets for a user
     */
    List<Wallet> findByUserIdAndStatus(UUID userId, WalletStatus status);

    /**
     * Find a wallet by its external ID
     */
    Optional<Wallet> findByExternalId(String externalId);

    /**
     * Find a wallet for a user by currency
     */
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);

    /**
     * Find a wallet with pessimistic lock for write operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") UUID id);
}