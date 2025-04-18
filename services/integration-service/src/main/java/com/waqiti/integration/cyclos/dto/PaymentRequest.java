package com.waqiti.integration.cyclos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request to perform a payment in Cyclos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private String amount;
    private String currency;
    private String description;
    private String type; // Payment type identifier
    private PaymentDestination to;
    private CustomFields customValues;
}
