package com.waqiti.integration.fineract.api;

import com.waqiti.integration.fineract.dto.*;
import com.waqiti.integration.config.FeignClientConfig;
import com.waqiti.integration.fineract.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "fineractApi", url = "${fineract.api.url}",
        configuration = FeignClientConfig.class)
public interface FineractApiClient {

    @PostMapping(value = "/clients", consumes = MediaType.APPLICATION_JSON_VALUE)
    ClientResponse createClient(@RequestHeader("Authorization") String authHeader,
                                @RequestHeader("Fineract-Platform-TenantId") String tenantId,
                                @RequestBody ClientRequest request);

    @GetMapping(value = "/clients/{clientId}")
    ClientResponse getClient(@RequestHeader("Authorization") String authHeader,
                             @RequestHeader("Fineract-Platform-TenantId") String tenantId,
                             @PathVariable("clientId") Long clientId);

    @PostMapping(value = "/savingsaccounts", consumes = MediaType.APPLICATION_JSON_VALUE)
    SavingsAccountResponse createSavingsAccount(@RequestHeader("Authorization") String authHeader,
                                                @RequestHeader("Fineract-Platform-TenantId") String tenantId,
                                                @RequestBody SavingsAccountRequest request);

    @GetMapping(value = "/savingsaccounts/{accountId}")
    SavingsAccountResponse getSavingsAccount(@RequestHeader("Authorization") String authHeader,
                                             @RequestHeader("Fineract-Platform-TenantId") String tenantId,
                                             @PathVariable("accountId") Long accountId);

    @PostMapping(value = "/savingsaccounts/{accountId}/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    TransactionResponse createTransaction(@RequestHeader("Authorization") String authHeader,
                                          @RequestHeader("Fineract-Platform-TenantId") String tenantId,
                                          @PathVariable("accountId") Long accountId,
                                          @RequestParam("command") String command,
                                          @RequestBody TransactionRequest request);
}