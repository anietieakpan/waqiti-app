package com.p2pfinance.payment.client;

import com.p2pfinance.payment.client.dto.TransferRequest;
import com.p2pfinance.payment.client.dto.TransferResponse;
import com.p2pfinance.payment.client.dto.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "wallet-service", url = "${wallet-service.url}")
public interface WalletServiceClient {
    
    @GetMapping("/api/v1/wallets/{walletId}")
    WalletResponse getWallet(@PathVariable UUID walletId);
    
    @GetMapping("/api/v1/wallets/user/{userId}")
    List<WalletResponse> getUserWallets(@PathVariable UUID userId);
    
    @PostMapping("/api/v1/wallets/transfer")
    TransferResponse transfer(@RequestBody TransferRequest request);
}