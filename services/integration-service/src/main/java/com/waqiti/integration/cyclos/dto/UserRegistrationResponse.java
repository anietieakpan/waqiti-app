package com.waqiti.integration.cyclos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Response from user registration in Cyclos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistrationResponse {
    private String id;
    private String name;
    private String username;
    private String email;
    private String status;
}
