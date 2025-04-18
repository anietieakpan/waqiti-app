package com.waqiti.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID; /**
 * Request to create a user in the external system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private UUID userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String externalSystem; // "FINERACT" or "CYCLOS"
}
