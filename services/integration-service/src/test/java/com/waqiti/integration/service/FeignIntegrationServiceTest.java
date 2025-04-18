// File: integration-service/src/test/java/com/waqiti/integration/service/FeignIntegrationServiceTest.java
package com.waqiti.integration.service;

import com.waqiti.integration.api.FineractApiClient;
import com.waqiti.integration.api.CyclosApiClient;
import com.waqiti.integration.dto.UserRegistrationRequest;
import com.waqiti.wallet.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeignIntegrationServiceTest {

    @Mock
    private FineractApiClient fineractClient;

    @Mock
    private CyclosApiClient cyclosClient;

    @InjectMocks
    private FeignIntegrationService integrationService;

    private UUID userId;
    private Wallet sourceWallet;
    private Wallet targetWallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        sourceWallet = new Wallet();
        ReflectionTestUtils.setField(sourceWallet, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(sourceWallet, "externalId", "ext-src-123");
        ReflectionTestUtils.setField(sourceWallet, "walletType", "FINERACT");
        ReflectionTestUtils.setField(sourceWallet, "accountType", "SAVINGS");
        ReflectionTestUtils.setField(sourceWallet, "currency", "USD");

        targetWallet = new Wallet();
        ReflectionTestUtils.setField(targetWallet, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(targetWallet, "externalId", "ext-tgt-456");
        ReflectionTestUtils.setField(targetWallet, "walletType", "FINERACT");
        ReflectionTestUtils.setField(targetWallet, "accountType", "SAVINGS");
        ReflectionTestUtils.setField(targetWallet, "currency", "USD");
    }

    @Test
    void testCreateWallet_Fineract_Success() {
        // Arrange
        String expectedExternalId = "fineract-account-123";
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", expectedExternalId);

        when(fineractClient.createSavingsAccount(any())).thenReturn(response);

        // Act
        String externalId = integrationService.createWallet(userId, "FINERACT", "SAVINGS", "USD");

        // Assert
        assertEquals(expectedExternalId, externalId);
        verify(fineractClient).createSavingsAccount(any());
        verifyNoInteractions(cyclosClient);
    }

    @Test
    void testCreateWallet_Cyclos_Success() {
        // Arrange
        String expectedExternalId = "cyclos-account-456";
        Map<String, Object> response = new HashMap<>();
        response.put("id", expectedExternalId);

        when(cyclosClient.createAccount(any())).thenReturn(response);

        // Act
        String externalId = integrationService.createWallet(userId, "CYCLOS", "CHECKING", "USD");

        // Assert
        assertEquals(expectedExternalId, externalId);
        verify(cyclosClient).createAccount(any());
        verifyNoInteractions(fineractClient);
    }

    @Test
    void testTransferBetweenWallets_Fineract_Success() {
        // Arrange
        String expectedTransactionId = "tx-789";
        Map<String, Object> response = new HashMap<>();
        response.put("transactionId", expectedTransactionId);

        when(fineractClient.transferFunds(any())).thenReturn(response);

        BigDecimal amount = new BigDecimal("100.00");

        // Act
        String transactionId = integrationService.transferBetweenWallets(sourceWallet, targetWallet, amount);

        // Assert
        assertEquals(expectedTransactionId, transactionId);
        verify(fineractClient).transferFunds(any());
    }

    @Test
    void testGetWalletBalance_Fineract_Success() {
        // Arrange
        BigDecimal expectedBalance = new BigDecimal("500.00");
        Map<String, Object> response = new HashMap<>();
        response.put("balance", expectedBalance);

        when(fineractClient.getAccountBalance(anyString())).thenReturn(response);

        // Act
        BigDecimal balance = integrationService.getWalletBalance(sourceWallet);

        // Assert
        assertEquals(expectedBalance, balance);
        verify(fineractClient).getAccountBalance("ext-src-123");
    }
}