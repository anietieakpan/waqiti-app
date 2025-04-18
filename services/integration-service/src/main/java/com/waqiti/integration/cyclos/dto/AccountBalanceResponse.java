package com.waqiti.integration.cyclos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Response for account balance query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBalanceResponse {
    private String availableBalance;
    private String reservedAmount;
    private String creditLimit;
    private String upperCreditLimit;
    private String currency;
}
