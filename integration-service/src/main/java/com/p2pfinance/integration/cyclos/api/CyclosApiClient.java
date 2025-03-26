package com.p2pfinance.integration.cyclos.api;

import com.p2pfinance.integration.cyclos.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "cyclosApi", url = "${cyclos.api.url}",
        configuration = com.p2pfinance.integration.config.FeignClientConfig.class)
public interface CyclosApiClient {

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    UserRegistrationResponse createUser(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody UserRegistrationRequest request);

    @GetMapping(value = "/users/{username}")
    UserRegistrationResponse getUserByUsername(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable("username") String username);

    @PostMapping(value = "/users/{username}/accounts", consumes = MediaType.APPLICATION_JSON_VALUE)
    AccountResponse createAccount(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable("username") String username,
                                  @RequestBody AccountCreationRequest request);

    @GetMapping(value = "/users/{username}/accounts")
    List<AccountResponse> getAccounts(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable("username") String username);

    @GetMapping(value = "/users/{username}/accounts/{accountId}")
    AccountBalanceResponse getAccountBalance(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable("username") String username,
                                             @PathVariable("accountId") String accountId);

    @PostMapping(value = "/users/{username}/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse performPayment(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("username") String username,
                                   @RequestBody PaymentRequest request);
}