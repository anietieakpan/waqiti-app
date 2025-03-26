package com.p2pfinance.integration.cyclos.service;

import com.p2pfinance.integration.cyclos.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyclosIntegrationService {
    private final WebClient cyclosWebClient;

    @CircuitBreaker(name = "cyclosApi", fallbackMethod = "createUserFallback")
    @Retry(name = "cyclosApi")
    public UserRegistrationResponse createUser(UserRegistrationRequest request) {
        log.info("Creating user in Cyclos: {}", request.getName());

        try {
            return cyclosWebClient.post()
                    .uri("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserRegistrationResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating user in Cyclos", e);
            throw new RuntimeException("Failed to create user in Cyclos", e);
        }
    }

    private UserRegistrationResponse createUserFallback(UserRegistrationRequest request, Throwable t) {
        log.warn("Fallback for createUser executed due to: {}", t.getMessage());
        // Return a dummy response
        UserRegistrationResponse fallbackResponse = new UserRegistrationResponse();
        fallbackResponse.setId("fallback-id");
        return fallbackResponse;
    }

    @CircuitBreaker(name = "cyclosApi", fallbackMethod = "createAccountFallback")
    @Retry(name = "cyclosApi")
    public AccountResponse createAccount(String userId, AccountCreationRequest request) {
        log.info("Creating account in Cyclos for user: {}", userId);

        try {
            return cyclosWebClient.post()
                    .uri("/users/{userId}/accounts", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AccountResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating account in Cyclos", e);
            throw new RuntimeException("Failed to create account in Cyclos", e);
        }
    }

    private AccountResponse createAccountFallback(String userId, AccountCreationRequest request, Throwable t) {
        log.warn("Fallback for createAccount executed due to: {}", t.getMessage());
        // Return a dummy response
        AccountResponse fallbackResponse = new AccountResponse();
        fallbackResponse.setId("fallback-account-id");
        return fallbackResponse;
    }

    @CircuitBreaker(name = "cyclosApi", fallbackMethod = "getAccountBalanceFallback")
    @Retry(name = "cyclosApi")
    public AccountBalanceResponse getAccountBalance(String userId, String accountId) {
        log.info("Retrieving account balance for user: {}, account: {}", userId, accountId);

        try {
            return cyclosWebClient.get()
                    .uri("/users/{userId}/accounts/{accountId}", userId, accountId)
                    .retrieve()
                    .bodyToMono(AccountBalanceResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error retrieving account balance from Cyclos", e);
            throw new RuntimeException("Failed to retrieve account balance from Cyclos", e);
        }
    }

    private AccountBalanceResponse getAccountBalanceFallback(String userId, String accountId, Throwable t) {
        log.warn("Fallback for getAccountBalance executed due to: {}", t.getMessage());
        // Return a dummy response
        AccountBalanceResponse fallbackResponse = new AccountBalanceResponse();
        fallbackResponse.setAvailableBalance("0.00");
        return fallbackResponse;
    }

    @CircuitBreaker(name = "cyclosApi", fallbackMethod = "performPaymentFallback")
    @Retry(name = "cyclosApi")
    public PaymentResponse performPayment(String userId, PaymentRequest request) {
        log.info("Performing payment from user: {}, amount: {}", userId, request.getAmount());

        try {
            return cyclosWebClient.post()
                    .uri("/users/{userId}/payments", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error performing payment in Cyclos", e);
            throw new RuntimeException("Failed to perform payment in Cyclos", e);
        }
    }

    private PaymentResponse performPaymentFallback(String userId, PaymentRequest request, Throwable t) {
        log.warn("Fallback for performPayment executed due to: {}", t.getMessage());
        // Return a dummy response
        PaymentResponse fallbackResponse = new PaymentResponse();
        fallbackResponse.setId("fallback-payment-id");
        fallbackResponse.setStatus("FAILED");
        return fallbackResponse;
    }
}