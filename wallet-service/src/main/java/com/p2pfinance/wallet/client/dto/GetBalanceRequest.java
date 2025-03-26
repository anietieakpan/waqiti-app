package com.p2pfinance.wallet.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request to get the balance of a wallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBalanceRequest {
    private String externalId;
    private String walletType; // "FINERACT" or "CYCLOS"
}
