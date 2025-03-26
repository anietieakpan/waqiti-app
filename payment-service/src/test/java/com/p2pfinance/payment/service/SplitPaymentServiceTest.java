/**
 * File: ./payment-service/src/test/java/com/p2pfinance/payment/service/SplitPaymentServiceTest.java
 */
package com.p2pfinance.payment.service;

import com.p2pfinance.payment.client.UserServiceClient;
import com.p2pfinance.payment.client.WalletServiceClient;
import com.p2pfinance.payment.client.dto.UserResponse;
import com.p2pfinance.payment.client.dto.WalletResponse;
import com.p2pfinance.payment.client.dto.TransferResponse;
import com.p2pfinance.payment.domain.SplitPayment;
import com.p2pfinance.payment.domain.SplitPaymentParticipant;
import com.p2pfinance.payment.domain.SplitPaymentStatus;
import com.p2pfinance.payment.dto.CreateSplitPaymentRequest;
import com.p2pfinance.payment.dto.AddParticipantRequest;
import com.p2pfinance.payment.dto.PaySplitShareRequest;
import com.p2pfinance.payment.dto.SplitPaymentResponse;
import com.p2pfinance.payment.dto.SplitPaymentParticipantRequest;
import com.p2pfinance.payment.repository.SplitPaymentRepository;
import com.p2pfinance.payment.repository.SplitPaymentParticipantRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SplitPaymentServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SplitPaymentRepository splitPaymentRepository;

    @Mock
    private SplitPaymentParticipantRepository participantRepository;

    @Mock
    private UserServiceClient userClient;

    @Mock
    private WalletServiceClient walletClient;

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private SplitPaymentService splitPaymentService;

    private UUID organizerId;
    private UUID participantId;
    private UUID initialParticipantId;
    private UUID splitPaymentId;
    private UUID walletId;
    private UUID transactionId;
    private SplitPayment splitPayment;
    private SplitPaymentParticipant participant;
    private CreateSplitPaymentRequest createRequest;
    private AddParticipantRequest addParticipantRequest;
    private PaySplitShareRequest paySplitShareRequest;

    @BeforeEach
    void setUp() {
        // Set up required fields in the service
        ReflectionTestUtils.setField(splitPaymentService, "kafkaTemplate", kafkaTemplate);
        ReflectionTestUtils.setField(splitPaymentService, "objectMapper", objectMapper);

        // Generate UUIDs for testing
        organizerId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        initialParticipantId = UUID.randomUUID();
        splitPaymentId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        // Create a request for testing
        createRequest = new CreateSplitPaymentRequest();
        createRequest.setTitle("Dinner");
        createRequest.setDescription("Split dinner bill");
        createRequest.setTotalAmount(new BigDecimal("100.00"));
        createRequest.setCurrency("USD");

        // Add participants to request with the FULL amount to satisfy validation
        List<SplitPaymentParticipantRequest> participants = Arrays.asList(
                new SplitPaymentParticipantRequest(initialParticipantId, new BigDecimal("100.00"))
        );
        createRequest.setParticipants(participants);

        // Create split payment for testing
        splitPayment = SplitPayment.create(
                organizerId,
                "Dinner",
                "Split dinner bill",
                new BigDecimal("100.00"),
                "USD",
                7 // 7 days expiry
        );
        ReflectionTestUtils.setField(splitPayment, "id", splitPaymentId);

        // Create participant for testing
        participant = SplitPaymentParticipant.create(
                splitPayment,
                participantId,
                new BigDecimal("20.00")
        );
        ReflectionTestUtils.setField(participant, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(participant, "paid", false);

        // Set up participants list for the split payment
        List<SplitPaymentParticipant> participantList = new ArrayList<>();
        participantList.add(participant);
        ReflectionTestUtils.setField(splitPayment, "participants", participantList);

        // Create AddParticipantRequest for testing
        addParticipantRequest = new AddParticipantRequest();
        addParticipantRequest.setUserId(participantId);
        addParticipantRequest.setAmount(new BigDecimal("20.00"));

        // Create PaySplitShareRequest for testing
        paySplitShareRequest = new PaySplitShareRequest();
        paySplitShareRequest.setSourceWalletId(walletId);

        // Mock responses for UserServiceClient
        UserResponse userResponse = new UserResponse();
        userResponse.setId(participantId);
        userResponse.setUsername("participant");

        // Set up wallet response
        WalletResponse walletResponse = new WalletResponse();
        walletResponse.setId(walletId);
        walletResponse.setUserId(participantId);
        walletResponse.setCurrency("USD");
        walletResponse.setBalance(new BigDecimal("100.00"));
        walletResponse.setStatus("ACTIVE");

        // Mock common responses
        when(userClient.getUser(any(UUID.class))).thenReturn(userResponse);

        // IMPORTANT: Mock the getUsers method that validates participants
        when(userClient.getUsers(anyList())).thenReturn(
                Arrays.asList(userResponse)
        );

        when(walletClient.getUserWallets(any(UUID.class))).thenReturn(Arrays.asList(walletResponse));
        when(walletClient.getWallet(walletId)).thenReturn(walletResponse);

        // Set up transfer response
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setId(transactionId);
        when(walletClient.transfer(any())).thenReturn(transferResponse);
    }

    @Test
    void testCreateSplitPayment_Success() {
        // Set up specific mocks for this test
        // Make sure userClient.getUsers returns the participant we're adding
        UserResponse userResponse = new UserResponse();
        userResponse.setId(initialParticipantId);
        when(userClient.getUsers(anyList())).thenReturn(Arrays.asList(userResponse));

        when(splitPaymentRepository.save(any(SplitPayment.class))).thenAnswer(inv -> {
            SplitPayment sp = inv.getArgument(0);
            ReflectionTestUtils.setField(sp, "id", splitPaymentId);
            return sp;
        });

        // Mock participant creation
        when(participantRepository.save(any(SplitPaymentParticipant.class))).thenReturn(participant);

        // Act
        SplitPaymentResponse response = splitPaymentService.createSplitPayment(organizerId, createRequest);

        // Assert
        assertNotNull(response);
        assertEquals(splitPaymentId, response.getId());
        assertEquals(organizerId, response.getOrganizerId());
        assertEquals("Dinner", response.getTitle());
        assertEquals(new BigDecimal("100.00"), response.getTotalAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(SplitPaymentStatus.ACTIVE.name(), response.getStatus());

        verify(splitPaymentRepository, times(2)).save(any(SplitPayment.class));
    }

    @Test
    void testAddParticipant_Success() {
        // Create a new participant ID
        UUID newParticipantId = UUID.randomUUID();

        // Create a user response for the new participant
        UserResponse newUserResponse = new UserResponse();
        newUserResponse.setId(newParticipantId);
        newUserResponse.setUsername("new_participant");

        // Set up the request with the new participant ID
        AddParticipantRequest newRequest = new AddParticipantRequest();
        newRequest.setUserId(newParticipantId);
        newRequest.setAmount(new BigDecimal("20.00"));

        // Create split payment for test
        SplitPayment testSplitPayment = SplitPayment.create(
                organizerId,
                "Test Payment",
                "Test Description",
                new BigDecimal("100.00"),
                "USD",
                7
        );
        ReflectionTestUtils.setField(testSplitPayment, "id", splitPaymentId);
        ReflectionTestUtils.setField(testSplitPayment, "participants", new ArrayList<>());

        // Mock repository responses
        when(splitPaymentRepository.findById(splitPaymentId)).thenReturn(Optional.of(testSplitPayment));
        when(participantRepository.existsBySplitPaymentIdAndUserId(splitPaymentId, newParticipantId)).thenReturn(false);
        when(userClient.getUser(newParticipantId)).thenReturn(newUserResponse);
        when(splitPaymentRepository.save(any(SplitPayment.class))).thenReturn(testSplitPayment);

        // Act
        SplitPaymentResponse response = splitPaymentService.addParticipant(organizerId, splitPaymentId, newRequest);

        // Assert
        assertNotNull(response);
        verify(splitPaymentRepository).findById(splitPaymentId);
        verify(userClient, atLeast(1)).getUser(newParticipantId);
        verify(participantRepository).existsBySplitPaymentIdAndUserId(splitPaymentId, newParticipantId);
        verify(splitPaymentRepository).save(any(SplitPayment.class));

        // We shouldn't expect participantRepository.save to be called directly
        // verify(participantRepository).save(any(SplitPaymentParticipant.class));
    }




    @Test
    void testPayShare_Success() {
        // Make sure participant is not marked as paid
        ReflectionTestUtils.setField(participant, "paid", false);

        // Set up repository mocks
        when(splitPaymentRepository.findById(splitPaymentId)).thenReturn(Optional.of(splitPayment));
        when(participantRepository.findBySplitPaymentIdAndUserId(splitPaymentId, participantId))
                .thenReturn(Optional.of(participant));
        when(splitPaymentRepository.save(any(SplitPayment.class))).thenReturn(splitPayment);

        // Act
        SplitPaymentResponse response = splitPaymentService.payShare(participantId, splitPaymentId, paySplitShareRequest);

        // Assert
        assertNotNull(response);
        verify(splitPaymentRepository).findById(splitPaymentId);
        verify(participantRepository).findBySplitPaymentIdAndUserId(splitPaymentId, participantId);
        verify(walletClient).getWallet(walletId);
        verify(walletClient).transfer(any());
    }

    @Test
    void testGetSplitPaymentById_Success() {
        // Set up repository mock
        when(splitPaymentRepository.findById(splitPaymentId)).thenReturn(Optional.of(splitPayment));

        // Act
        SplitPaymentResponse response = splitPaymentService.getSplitPaymentById(splitPaymentId);

        // Assert
        assertNotNull(response);
        assertEquals(splitPaymentId, response.getId());
        assertEquals(1, response.getParticipants().size());

        verify(splitPaymentRepository).findById(splitPaymentId);
    }
}