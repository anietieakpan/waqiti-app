package com.p2pfinance.integration.fineract.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.ApiClient;
import org.apache.fineract.client.api.ClientApi;
import org.apache.fineract.client.api.SavingsAccountsApi;
import org.apache.fineract.client.models.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineractIntegrationService {
    private final ApiClient fineractApiClient;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @CircuitBreaker(name = "fineractApi", fallbackMethod = "createClientFallback")
    @Retry(name = "fineractApi")
    public PostClientsResponse createClient(String firstName, String lastName, String externalId) {
        log.info("Creating client in Fineract: {} {}", firstName, lastName);

        ClientApi clientApi = new ClientApi(fineractApiClient);

        PostClientsRequest request = new PostClientsRequest()
                .firstName(firstName)
                .lastName(lastName)
                .externalId(externalId)
                .active(true)
                .locale("en")
                .dateFormat("dd MMMM yyyy")
                .activationDate(LocalDate.now().format(DATE_FORMAT));

        try {
            return clientApi.create11(request);
        } catch (Exception e) {
            log.error("Error creating client in Fineract", e);
            throw new RuntimeException("Failed to create client in Fineract", e);
        }
    }

    private PostClientsResponse createClientFallback(String firstName, String lastName, String externalId,
            Throwable t) {
        log.warn("Fallback for createClient executed due to: {}", t.getMessage());
        // Return a dummy response or handle the fallback scenario
        PostClientsResponse fallbackResponse = new PostClientsResponse();
        fallbackResponse.setClientId(0L);
        fallbackResponse.setResourceId(0L);
        return fallbackResponse;
    }

    @CircuitBreaker(name = "fineractApi", fallbackMethod = "createSavingsAccountFallback")
    @Retry(name = "fineractApi")
    public PostSavingsResponse createSavingsAccount(Long clientId, String currencyCode) {
        log.info("Creating savings account for client: {}", clientId);

        SavingsAccountsApi savingsApi = new SavingsAccountsApi(fineractApiClient);

        PostSavingsAccountsRequest request = new PostSavingsAccountsRequest()
                .clientId(clientId)
                .productId(1L) // Assuming product ID 1 exists in Fineract
                .locale("en")
                .dateFormat("dd MMMM yyyy")
                .submittedOnDate(LocalDate.now().format(DATE_FORMAT))
                .currencyCode(currencyCode)
                .nominalAnnualInterestRate(0.0);

        try {
            return savingsApi.create18(request);
        } catch (Exception e) {
            log.error("Error creating savings account in Fineract", e);
            throw new RuntimeException("Failed to create savings account in Fineract", e);
        }
    }

    private PostSavingsResponse createSavingsAccountFallback(Long clientId, String currencyCode, Throwable t) {
        log.warn("Fallback for createSavingsAccount executed due to: {}", t.getMessage());
        // Return a dummy response or handle the fallback scenario
        PostSavingsResponse fallbackResponse = new PostSavingsResponse();
        fallbackResponse.setSavingsId(0L);
        fallbackResponse.setResourceId(0L);
        return fallbackResponse;
    }

    @CircuitBreaker(name = "fineractApi", fallbackMethod = "getSavingsAccountFallback")
    @Retry(name = "fineractApi")
    public GetSavingsAccountsResponse getSavingsAccount(Long accountId) {
        log.info("Retrieving savings account: {}", accountId);

        SavingsAccountsApi savingsApi = new SavingsAccountsApi(fineractApiClient);

        try {
            return savingsApi.retrieveOne27(accountId, false, "all");
        } catch (Exception e) {
            log.error("Error retrieving savings account from Fineract", e);
            throw new RuntimeException("Failed to retrieve savings account from Fineract", e);
        }
    }

    private GetSavingsAccountsResponse getSavingsAccountFallback(Long accountId, Throwable t) {
        log.warn("Fallback for getSavingsAccount executed due to: {}", t.getMessage());
        // Return a dummy response or handle the fallback scenario
        return new GetSavingsAccountsResponse();
    }

    @CircuitBreaker(name = "fineractApi", fallbackMethod = "transferFundsFallback")
    @Retry(name = "fineractApi")
    public PostSavingsAccountsAccountIdTransactionsResponse transferFunds(
            Long fromAccountId, Long toAccountId, Double amount, String currencyCode) {
        log.info("Transferring {} {} from account {} to account {}",
                amount, currencyCode, fromAccountId, toAccountId);

        SavingsAccountsApi savingsApi = new SavingsAccountsApi(fineractApiClient);

        // First, withdraw from source account
        PostSavingsAccountsAccountIdTransactionsRequest withdrawRequest = new PostSavingsAccountsAccountIdTransactionsRequest()
                .transactionDate(LocalDate.now().format(DATE_FORMAT))
                .transactionAmount(amount)
                .paymentTypeId(1L) // Assume payment type ID 1 exists
                .locale("en")
                .dateFormat("dd MMMM yyyy");

        try {
            // Withdraw
            PostSavingsAccountsAccountIdTransactionsResponse withdrawResponse = savingsApi.transaction1(fromAccountId,
                    "withdrawal", withdrawRequest);

            // Deposit to target account
            PostSavingsAccountsAccountIdTransactionsRequest depositRequest = new PostSavingsAccountsAccountIdTransactionsRequest()
                    .transactionDate(LocalDate.now().format(DATE_FORMAT))
                    .transactionAmount(amount)
                    .paymentTypeId(1L)
                    .locale("en")
                    .dateFormat("dd MMMM yyyy");

            savingsApi.transaction1(toAccountId, "deposit", depositRequest);

            return withdrawResponse; // Return the withdrawal response as confirmation
        } catch (Exception e) {
            log.error("Error transferring funds in Fineract", e);
            throw new RuntimeException("Failed to transfer funds in Fineract", e);
        }
    }

    private PostSavingsAccountsAccountIdTransactionsResponse transferFundsFallback(
            Long fromAccountId, Long toAccountId, Double amount, String currencyCode, Throwable t) {
        log.warn("Fallback for transferFunds executed due to: {}", t.getMessage());
        // Return a dummy response
        return new PostSavingsAccountsAccountIdTransactionsResponse();
    }
}