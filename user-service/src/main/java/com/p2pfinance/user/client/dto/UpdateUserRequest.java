package com.p2pfinance.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request to update a user in the external system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String externalId;
    private String externalSystem; // "FINERACT" or "CYCLOS"
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
}
