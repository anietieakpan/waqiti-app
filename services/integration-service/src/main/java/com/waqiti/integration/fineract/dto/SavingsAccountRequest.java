package com.waqiti.integration.fineract.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsAccountRequest {
    private Long clientId;
    private Long productId;
    private String locale;
    private String dateFormat;
    private String submittedOnDate;
    private String currencyCode;
    private Double nominalAnnualInterestRate;
}
