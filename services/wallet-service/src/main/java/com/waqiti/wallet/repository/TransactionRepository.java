package com.waqiti.wallet.repository;

import com.waqiti.wallet.domain.Transaction;
import com.waqiti.wallet.domain.TransactionStatus;
import com.waqiti.wallet.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find a transaction by its external ID
     */
    Optional<Transaction> findByExternalId(String externalId);

    /**
     * Find transactions for a user (either as source or target)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWalletId IN " +
            "(SELECT w.id FROM Wallet w WHERE w.userId = :userId) OR " +
            "t.targetWalletId IN (SELECT w.id FROM Wallet w WHERE w.userId = :userId)")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find transactions for a specific wallet
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceWalletId = :walletId OR t.targetWalletId = :walletId")
    Page<Transaction> findByWalletId(@Param("walletId") UUID walletId, Pageable pageable);

    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Find transactions by type
     */
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    /**
     * Find transactions by date range
     */
    Page<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Find pending transactions older than a specific time
     */
    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime beforeTime);
}