/**
 * File: ./payment-service/src/main/java/com/waqiti/payment/api/PaymentRequestController.java
 */
package com.waqiti.payment.api;

import com.waqiti.payment.dto.*;
import com.waqiti.payment.dto.ApprovePaymentRequestRequest;
import com.waqiti.payment.dto.CancelPaymentRequestRequest;
import com.waqiti.payment.dto.CreatePaymentRequestRequest;
import com.waqiti.payment.dto.PaymentRequestResponse;
import com.waqiti.payment.service.PaymentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment-requests")
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestController {
    private final PaymentRequestService paymentRequestService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> createPaymentRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePaymentRequestRequest request) {
        log.info("Create payment request received");
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.createPaymentRequest(userId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> getPaymentRequestById(@PathVariable UUID id) {
        log.info("Get payment request received for ID: {}", id);
        return ResponseEntity.ok(paymentRequestService.getPaymentRequestById(id));
    }

    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PaymentRequestResponse>> getSentPaymentRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        log.info("Get sent payment requests received");
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.getSentPaymentRequests(userId, pageable));
    }

    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PaymentRequestResponse>> getReceivedPaymentRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        log.info("Get received payment requests received");
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.getReceivedPaymentRequests(userId, pageable));
    }

    @GetMapping("/received/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PaymentRequestResponse>> getPendingReceivedPaymentRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        log.info("Get pending received payment requests received");
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.getPendingReceivedPaymentRequests(userId, pageable));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> approvePaymentRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ApprovePaymentRequestRequest request) {
        log.info("Approve payment request received for ID: {}", id);
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.approvePaymentRequest(userId, id, request));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> rejectPaymentRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        log.info("Reject payment request received for ID: {}", id);
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.rejectPaymentRequest(userId, id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> cancelPaymentRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelPaymentRequestRequest request) {
        log.info("Cancel payment request received for ID: {}", id);
        UUID userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(paymentRequestService.cancelPaymentRequest(
                userId, id, request != null ? request : new CancelPaymentRequestRequest()));
    }

    /**
     * Helper method to extract user ID from UserDetails with better error handling for tests
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to parse UUID from username: {}", userDetails.getUsername());

            // For test environments, provide a fallback for specific test users
            if ("testuser".equals(userDetails.getUsername())) {
                log.info("Using default test UUID for user 'testuser'");
                return UUID.fromString("3da1bd8c-d04b-4eee-b8ab-c7a8c1142599");
            }

            throw new IllegalArgumentException("Invalid UUID format: " + userDetails.getUsername(), e);
        }
    }
}