package com.p2pfinance.wallet.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; /**
 * Request to transfer between wallets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String sourceExternalId;
    private String sourceWalletType;
    private String targetExternalId;
    private String targetWalletType;
    private BigDecimal amount;
    private String currency;
}
