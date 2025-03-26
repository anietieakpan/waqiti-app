/**
 * File: ./payment-service/src/test/java/com/p2pfinance/payment/service/PaymentServiceIntegrationTest.java
 */
package com.p2pfinance.payment.service;

import com.p2pfinance.payment.client.WalletServiceClient;
import com.p2pfinance.payment.client.UserServiceClient;
import com.p2pfinance.payment.client.dto.WalletResponse;
import com.p2pfinance.payment.client.dto.TransferResponse;
import com.p2pfinance.payment.client.dto.UserResponse;
import com.p2pfinance.payment.config.TestConfig;
import com.p2pfinance.payment.domain.PaymentRequest;
import com.p2pfinance.payment.domain.PaymentRequestStatus;
import com.p2pfinance.payment.dto.CreatePaymentRequestRequest;
import com.p2pfinance.payment.dto.ApprovePaymentRequestRequest;
import com.p2pfinance.payment.dto.PaymentRequestResponse;
import com.p2pfinance.payment.repository.PaymentRequestRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import(TestConfig.class)
public class PaymentServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("payment-integration-test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // Disable Flyway to avoid circular dependency
        registry.add("spring.flyway.enabled", () -> "false");

        // Configure JPA
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Disable CSRF for tests
        registry.add("spring.security.csrf.enabled", () -> "false");
    }

    @Autowired
    private PaymentRequestService paymentRequestService;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    // Add directly autowired reference to MeterRegistry
    @Autowired
    private MeterRegistry meterRegistry;

    @MockBean
    private WalletServiceClient walletClient;

    @MockBean
    private UserServiceClient userClient;

    private UUID requestorId;
    private UUID recipientId;
    private UUID walletId;
    private UUID transactionId;
    private CreatePaymentRequestRequest createRequest;
    private ApprovePaymentRequestRequest approveRequest;

    @BeforeEach
    void setUp() {

        requestorId = UUID.randomUUID();
        recipientId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        // Setup request data
        createRequest = new CreatePaymentRequestRequest();
        createRequest.setRecipientId(recipientId);
        createRequest.setAmount(new BigDecimal("100.00"));
        createRequest.setCurrency("USD");
        createRequest.setDescription("Test payment request");
        createRequest.setExpiryHours(48);

        approveRequest = new ApprovePaymentRequestRequest();
        // Using ReflectionTestUtils for field that might have a different name
        ReflectionTestUtils.setField(approveRequest, "sourceWalletId", walletId);

        // Mock user client
        UserResponse userResponse = new UserResponse();
        userResponse.setId(recipientId);
        userResponse.setUsername("recipient");
        userResponse.setEmail("recipient@example.com");
        when(userClient.getUser(any(UUID.class))).thenReturn(userResponse);

        // Mock wallet client - IMPORTANT: wallet must belong to recipient (who approves payment)
        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setId(walletId);
        walletResponse.setUserId(recipientId); // This is key - wallet belongs to recipient
        walletResponse.setCurrency("USD");
        walletResponse.setBalance(new BigDecimal("1000.00"));
        walletResponse.setStatus("ACTIVE");
        when(walletClient.getUserWallets(recipientId)).thenReturn(Arrays.asList(walletResponse));
        when(walletClient.getWallet(walletId)).thenReturn(walletResponse);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setId(transactionId);
        when(walletClient.transfer(any())).thenReturn(transferResponse);

        WalletResponse requestorWallet = new WalletResponse();
        requestorWallet.setId(UUID.randomUUID());
        requestorWallet.setUserId(requestorId);
        requestorWallet.setCurrency("USD");
        requestorWallet.setBalance(new BigDecimal("1000.00"));
        requestorWallet.setStatus("ACTIVE");

        // Setup recipient wallet (this is the wallet that will be used for approval)
        WalletResponse recipientWallet = new WalletResponse();
        recipientWallet.setId(walletId);
        recipientWallet.setUserId(recipientId);
        recipientWallet.setCurrency("USD");
        recipientWallet.setBalance(new BigDecimal("1000.00"));
        recipientWallet.setStatus("ACTIVE");

        // Make sure each user gets their own wallet
        when(walletClient.getUserWallets(requestorId)).thenReturn(Arrays.asList(requestorWallet));
        when(walletClient.getUserWallets(recipientId)).thenReturn(Arrays.asList(recipientWallet));
        when(walletClient.getWallet(walletId)).thenReturn(recipientWallet);
    }

    @Test
    void testCompletePaymentRequestLifecycle() {
        // 1. Create payment request
        PaymentRequestResponse createResponse = paymentRequestService.createPaymentRequest(requestorId, createRequest);

        // Verify creation
        assertNotNull(createResponse);
        assertEquals(requestorId, createResponse.getRequestorId());
        assertEquals(recipientId, createResponse.getRecipientId());
        assertEquals(new BigDecimal("100.00"), createResponse.getAmount());
        assertEquals("USD", createResponse.getCurrency());
        assertEquals(PaymentRequestStatus.PENDING.name(), createResponse.getStatus());

        UUID paymentRequestId = createResponse.getId();

        // Verify saved in repository
        PaymentRequest savedRequest = paymentRequestRepository.findById(paymentRequestId).orElseThrow();
        assertEquals(PaymentRequestStatus.PENDING, savedRequest.getStatus());

        // 2. Approve payment request
        PaymentRequestResponse approveResponse = paymentRequestService.approvePaymentRequest(recipientId, paymentRequestId, approveRequest);

        // Verify approval
        assertNotNull(approveResponse);
        assertEquals(PaymentRequestStatus.APPROVED.name(), approveResponse.getStatus());
        assertEquals(transactionId, approveResponse.getTransactionId());

        // Verify updated in repository
        PaymentRequest updatedRequest = paymentRequestRepository.findById(paymentRequestId).orElseThrow();
        assertEquals(PaymentRequestStatus.APPROVED, updatedRequest.getStatus());
        assertEquals(transactionId, updatedRequest.getTransactionId());

        // Verify wallet service was called
        verify(walletClient).transfer(any());
    }

    @Test
    void testRejectPaymentRequest() {
        // 1. Create payment request
        PaymentRequestResponse createResponse = paymentRequestService.createPaymentRequest(requestorId, createRequest);
        UUID paymentRequestId = createResponse.getId();

        // 2. Reject payment request
        PaymentRequestResponse rejectResponse = paymentRequestService.rejectPaymentRequest(recipientId, paymentRequestId);

        // Verify rejection
        assertNotNull(rejectResponse);
        assertEquals(PaymentRequestStatus.REJECTED.name(), rejectResponse.getStatus());

        // Verify updated in repository
        PaymentRequest updatedRequest = paymentRequestRepository.findById(paymentRequestId).orElseThrow();
        assertEquals(PaymentRequestStatus.REJECTED, updatedRequest.getStatus());

        // Verify wallet service was NOT called
        verify(walletClient, never()).transfer(any());
    }
}