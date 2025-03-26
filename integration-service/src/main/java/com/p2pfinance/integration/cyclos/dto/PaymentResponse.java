package com.p2pfinance.integration.cyclos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Response from payment in Cyclos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    private String id;
    private String amount;
    private String currency;
    private String description;
    private String date;
    private String status;
    private String fromUser;
    private String toUser;
}
