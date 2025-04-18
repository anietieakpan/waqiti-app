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
public class ClientRequest {
    private String firstname;
    private String lastname;
    private String externalId;
    private boolean active;
    private String dateFormat;
    private String locale;
    private String activationDate;
}
