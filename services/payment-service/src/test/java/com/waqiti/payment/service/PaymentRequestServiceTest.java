/**
 * File: ./payment-service/src/test/java/com/waqiti/payment/service/PaymentRequestServiceTest.java
 */
package com.waqiti.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.common.event.EventPublisher;
import com.waqiti.payment.client.WalletServiceClient;
import com.waqiti.payment.client.UserServiceClient;
import com.waqiti.payment.client.dto.TransferResponse;
import com.waqiti.payment.client.dto.WalletResponse;
import com.waqiti.payment.client.dto.UserResponse;
import com.waqiti.payment.domain.PaymentRequest;
import com.waqiti.payment.domain.PaymentRequestStatus;
import com.waqiti.payment.dto.CreatePaymentRequestRequest;
import com.waqiti.payment.dto.ApprovePaymentRequestRequest;
import com.waqiti.payment.dto.PaymentRequestResponse;
import com.waqiti.payment.repository.PaymentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.Spy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRequestServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private WalletServiceClient walletClient;

    @Mock
    private UserServiceClient userClient;

    // Add a spy on SimpleMeterRegistry instead of a mock
    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private PaymentRequestService paymentRequestService;

    private UUID requestorId;
    private UUID recipientId;
    private UUID paymentRequestId;
    private UUID walletId;
    private PaymentRequest paymentRequest;
    private CreatePaymentRequestRequest createRequest;
    private ApprovePaymentRequestRequest approveRequest;
    private UserResponse userResponse;
    private List<WalletResponse> walletResponses;

    @BeforeEach
    void setUp() {
        // No need to set fields with ReflectionTestUtils since we're using @InjectMocks
        requestorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        paymentRequestId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        createRequest = new CreatePaymentRequestRequest();
        createRequest.setRecipientId(recipientId);
        createRequest.setAmount(new BigDecimal("100.00"));
        createRequest.setCurrency("USD");
        createRequest.setDescription("Test payment request");
        createRequest.setExpiryHours(48);

        // Use the factory method instead of constructor
        paymentRequest = PaymentRequest.create(
                requestorId,
                recipientId,
                new BigDecimal("100.00"),
                "USD",
                "Test payment request",
                48
        );
        ReflectionTestUtils.setField(paymentRequest, "id", paymentRequestId);

        approveRequest = new ApprovePaymentRequestRequest();
        approveRequest.setSourceWalletId(walletId);

        userResponse = new UserResponse();
        userResponse.setId(recipientId);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");

        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setId(walletId);
        walletResponse.setUserId(recipientId);
        walletResponse.setCurrency("USD");
        walletResponse.setStatus("ACTIVE");
        walletResponse.setBalance(new BigDecimal("500.00"));

        walletResponses = Arrays.asList(walletResponse);
    }

    @Test
    void testCreatePaymentRequest_Success() {
        // Arrange
        when(userClient.getUser(recipientId)).thenReturn(userResponse);
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(inv -> {
            PaymentRequest pr = inv.getArgument(0);
            ReflectionTestUtils.setField(pr, "id", paymentRequestId);
            return pr;
        });

        // Act
        PaymentRequestResponse response = paymentRequestService.createPaymentRequest(requestorId, createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(paymentRequestId, response.getId());
        assertEquals(requestorId, response.getRequestorId());
        assertEquals(recipientId, response.getRecipientId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals("Test payment request", response.getDescription());
        assertEquals(PaymentRequestStatus.PENDING.name(), response.getStatus());

        verify(userClient, atLeastOnce()).getUser(recipientId);
        verify(paymentRequestRepository).save(any(PaymentRequest.class));
    }

    @Test
    void testApprovePaymentRequest_Success() {
        // Arrange
        UUID transactionId = UUID.randomUUID();

        when(paymentRequestRepository.findById(paymentRequestId)).thenReturn(Optional.of(paymentRequest));
        when(walletClient.getUserWallets(requestorId)).thenReturn(walletResponses);
        when(walletClient.getWallet(walletId)).thenReturn(walletResponses.get(0));

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setId(transactionId);
        when(walletClient.transfer(any())).thenReturn(transferResponse);

        when(paymentRequestRepository.save(paymentRequest)).thenReturn(paymentRequest);

        // Act
        PaymentRequestResponse response = paymentRequestService.approvePaymentRequest(recipientId, paymentRequestId, approveRequest);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentRequestStatus.APPROVED.name(), response.getStatus());
        assertEquals(transactionId, response.getTransactionId());

        verify(paymentRequestRepository).findById(paymentRequestId);
        verify(walletClient).getUserWallets(requestorId);
        verify(walletClient).transfer(any());
        verify(paymentRequestRepository).save(paymentRequest);
    }

    @Test
    void testRejectPaymentRequest_Success() {
        // Arrange
        when(paymentRequestRepository.findById(paymentRequestId)).thenReturn(Optional.of(paymentRequest));
        when(paymentRequestRepository.save(paymentRequest)).thenReturn(paymentRequest);

        // Act
        PaymentRequestResponse response = paymentRequestService.rejectPaymentRequest(recipientId, paymentRequestId);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentRequestStatus.REJECTED.name(), response.getStatus());

        verify(paymentRequestRepository).findById(paymentRequestId);
        verify(paymentRequestRepository).save(paymentRequest);
    }
}