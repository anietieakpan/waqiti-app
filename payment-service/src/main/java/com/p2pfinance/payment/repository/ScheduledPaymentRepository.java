package com.p2pfinance.payment.repository;

import com.p2pfinance.payment.domain.ScheduledPayment;
import com.p2pfinance.payment.domain.ScheduledPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, UUID> {
    /**
     * Find scheduled payments by sender ID
     */
    Page<ScheduledPayment> findBySenderId(UUID senderId, Pageable pageable);
    
    /**
     * Find scheduled payments by sender ID and status
     */
    Page<ScheduledPayment> findBySenderIdAndStatus(UUID senderId, ScheduledPaymentStatus status, Pageable pageable);
    
    /**
     * Find scheduled payments by recipient ID
     */
    Page<ScheduledPayment> findByRecipientId(UUID recipientId, Pageable pageable);
    
    /**
     * Find scheduled payments by recipient ID and status
     */
    Page<ScheduledPayment> findByRecipientIdAndStatus(UUID recipientId, ScheduledPaymentStatus status, Pageable pageable);
    
    /**
     * Find active scheduled payments due for execution
     */
    @Query("SELECT sp FROM ScheduledPayment sp WHERE " +
           "sp.status = 'ACTIVE' AND " +
           "sp.nextExecutionDate <= :date")
    List<ScheduledPayment> findActivePaymentsDueForExecution(@Param("date") LocalDate date);
    
    /**
     * Find scheduled payments between two users
     */
    @Query("SELECT sp FROM ScheduledPayment sp WHERE " +
           "(sp.senderId = :user1Id AND sp.recipientId = :user2Id) OR " +
           "(sp.senderId = :user2Id AND sp.recipientId = :user1Id) " +
           "ORDER BY sp.createdAt DESC")
    Page<ScheduledPayment> findBetweenUsers(
            @Param("user1Id") UUID user1Id, 
            @Param("user2Id") UUID user2Id, 
            Pageable pageable);
}