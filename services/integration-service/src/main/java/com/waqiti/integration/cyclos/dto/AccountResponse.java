package com.waqiti.integration.cyclos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Response from account creation in Cyclos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {
    private String id;
    private String name;
    private String type;
    private String currency;
    private String status;
}
