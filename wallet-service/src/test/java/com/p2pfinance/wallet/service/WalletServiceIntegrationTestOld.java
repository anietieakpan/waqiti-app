// File: wallet-service/src/test/java/com/p2pfinance/wallet/service/WalletServiceIntegrationTest.java
package com.p2pfinance.wallet.service;

import com.p2pfinance.wallet.config.TestConfig;
import com.p2pfinance.wallet.domain.*;
import com.p2pfinance.wallet.dto.CreateWalletRequest;
import com.p2pfinance.wallet.dto.TransactionResponse;
import com.p2pfinance.wallet.dto.TransferRequest;
import com.p2pfinance.wallet.dto.WalletResponse;
import com.p2pfinance.wallet.repository.TransactionRepository;
import com.p2pfinance.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
public class WalletServiceIntegrationTestOld {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private IntegrationService integrationService;

    @MockBean
    private TransactionLogger transactionLogger;

    private UUID userId;
    private CreateWalletRequest createWalletRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setUserId(userId);
        createWalletRequest.setWalletType("FINERACT");
        createWalletRequest.setAccountType("SAVINGS");
        createWalletRequest.setCurrency("USD");

        // Mock the external integration service
        when(integrationService.createWallet(any(), any(), any(), any())).thenReturn("ext-123");
        when(integrationService.transferBetweenWallets(any(), any(), any())).thenReturn("tx-123");
        when(integrationService.getWalletBalance(any())).thenReturn(BigDecimal.ZERO);

        // Create a dynamic transaction factory
        when(transactionLogger.createTransactionAudit(
                any(UUID.class),
                any(UUID.class),
                any(BigDecimal.class),
                anyString(),
                any(TransactionType.class),
                anyString()
        )).thenAnswer(invocation -> {
            UUID sourceId = invocation.getArgument(0);
            UUID targetId = invocation.getArgument(1);
            BigDecimal amount = invocation.getArgument(2);
            String currency = invocation.getArgument(3);
            String description = invocation.getArgument(5);

            Transaction transaction = Transaction.createTransfer(
                    sourceId,
                    targetId,
                    amount,
                    currency,
                    description
            );

            // Add ID using reflection if needed
            ReflectionTestUtils.setField(transaction, "id", UUID.randomUUID());
            return transaction;
        });

        // Mock void methods
        doNothing().when(transactionLogger).logTransaction(any(Transaction.class));
        doNothing().when(transactionLogger).logWalletEvent(
                any(UUID.class), any(UUID.class), anyString(), any(BigDecimal.class), anyString(), any(UUID.class));
        doNothing().when(transactionLogger).logTransactionFailure(
                any(UUID.class), anyString(), anyString());
    }

    @Test
    void testCreateAndTransferBetweenWallets() {
        // Create source wallet
        WalletResponse sourceWalletResponse = walletService.createWallet(createWalletRequest);
        assertNotNull(sourceWalletResponse);
        assertEquals(userId, sourceWalletResponse.getUserId());

        // Create target wallet (for a different user)
        UUID otherUserId = UUID.randomUUID();
        createWalletRequest.setUserId(otherUserId);
        WalletResponse targetWalletResponse = walletService.createWallet(createWalletRequest);
        assertNotNull(targetWalletResponse);
        assertEquals(otherUserId, targetWalletResponse.getUserId());

        // Add funds to source wallet (mocking external deposit)
        Wallet sourceWallet = walletRepository.findById(sourceWalletResponse.getId()).orElseThrow();
        sourceWallet.updateBalance(new BigDecimal("1000.00"));
        walletRepository.save(sourceWallet);

        // Create transfer request
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceWalletId(sourceWalletResponse.getId());
        transferRequest.setTargetWalletId(targetWalletResponse.getId());
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setDescription("Test transfer");

        // Perform transfer
        TransactionResponse transactionResponse = walletService.transfer(transferRequest);

        // Verify transaction created correctly
        assertNotNull(transactionResponse);
        assertEquals(sourceWalletResponse.getId(), transactionResponse.getSourceWalletId());
        assertEquals(targetWalletResponse.getId(), transactionResponse.getTargetWalletId());
        assertEquals(new BigDecimal("100.00"), transactionResponse.getAmount());

        // Verify balances updated
        Wallet updatedSourceWallet = walletRepository.findById(sourceWalletResponse.getId()).orElseThrow();
        Wallet updatedTargetWallet = walletRepository.findById(targetWalletResponse.getId()).orElseThrow();

        assertEquals(new BigDecimal("900.00"), updatedSourceWallet.getBalance());
        assertEquals(new BigDecimal("100.00"), updatedTargetWallet.getBalance());
    }

    @Test
    void testFailedTransferDueToInsufficientFunds() {
        // Create source wallet
        WalletResponse sourceWalletResponse = walletService.createWallet(createWalletRequest);

        // Create target wallet
        UUID otherUserId = UUID.randomUUID();
        createWalletRequest.setUserId(otherUserId);
        WalletResponse targetWalletResponse = walletService.createWallet(createWalletRequest);

        // Add insufficient funds to source wallet
        Wallet sourceWallet = walletRepository.findById(sourceWalletResponse.getId()).orElseThrow();
        sourceWallet.updateBalance(new BigDecimal("50.00"));
        walletRepository.save(sourceWallet);

        // Create transfer request with amount greater than balance
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceWalletId(sourceWalletResponse.getId());
        transferRequest.setTargetWalletId(targetWalletResponse.getId());
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setDescription("Test transfer that should fail");

        // Attempt transfer and expect exception
        assertThrows(InsufficientBalanceException.class, () -> {
            walletService.transfer(transferRequest);
        });

        // Verify balances not changed
        Wallet updatedSourceWallet = walletRepository.findById(sourceWalletResponse.getId()).orElseThrow();
        Wallet updatedTargetWallet = walletRepository.findById(targetWalletResponse.getId()).orElseThrow();

        assertEquals(new BigDecimal("50.00"), updatedSourceWallet.getBalance());
        assertEquals(BigDecimal.ZERO, updatedTargetWallet.getBalance());
    }

    @Test
    void testGetWalletById() {
        // Create wallet
        WalletResponse createdWalletResponse = walletService.createWallet(createWalletRequest);

        // Get wallet by ID
        WalletResponse retrievedWalletResponse = walletService.getWallet(createdWalletResponse.getId());

        // Verify wallet retrieved correctly
        assertNotNull(retrievedWalletResponse);
        assertEquals(createdWalletResponse.getId(), retrievedWalletResponse.getId());
        assertEquals(userId, retrievedWalletResponse.getUserId());
    }

    @Test
    void testGetWalletsByUserId() {
        // Create multiple wallets for the same user
        walletService.createWallet(createWalletRequest);

        // Create a second wallet with different currency
        CreateWalletRequest secondWalletRequest = new CreateWalletRequest();
        secondWalletRequest.setUserId(userId);
        secondWalletRequest.setWalletType("FINERACT");
        secondWalletRequest.setAccountType("SAVINGS");
        secondWalletRequest.setCurrency("EUR");
        walletService.createWallet(secondWalletRequest);

        // Get wallets by user ID
        List<WalletResponse> wallets = walletService.getUserWallets(userId);

        // Verify both wallets retrieved
        assertEquals(2, wallets.size());
        assertTrue(wallets.stream().anyMatch(w -> w.getCurrency().equals("USD")));
        assertTrue(wallets.stream().anyMatch(w -> w.getCurrency().equals("EUR")));
    }

    @Test
    void testFreezeWallet() {
        // Create wallet
        WalletResponse createdWalletResponse = walletService.createWallet(createWalletRequest);

        // Freeze wallet
        WalletResponse frozenWalletResponse = walletService.freezeWallet(createdWalletResponse.getId(), "Test freeze");

        // Verify wallet frozen
        assertEquals(WalletStatus.FROZEN.name(), frozenWalletResponse.getStatus());

        // Verify wallet in repository is frozen
        Wallet wallet = walletRepository.findById(createdWalletResponse.getId()).orElseThrow();
        assertEquals(WalletStatus.FROZEN, wallet.getStatus());

        // Try to transfer from frozen wallet
        UUID otherUserId = UUID.randomUUID();
        createWalletRequest.setUserId(otherUserId);
        WalletResponse targetWalletResponse = walletService.createWallet(createWalletRequest);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceWalletId(createdWalletResponse.getId());
        transferRequest.setTargetWalletId(targetWalletResponse.getId());
        transferRequest.setAmount(new BigDecimal("10.00"));
        transferRequest.setDescription("Test transfer from frozen wallet");

        // Expect transfer to be denied
        assertThrows(WalletNotActiveException.class, () -> {
            walletService.transfer(transferRequest);
        });
    }
}