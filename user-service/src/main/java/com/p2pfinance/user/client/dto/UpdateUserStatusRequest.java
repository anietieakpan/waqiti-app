package com.p2pfinance.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * Request to update a user's status in the external system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private String externalId;
    private String externalSystem; // "FINERACT" or "CYCLOS"
    private String status;
}
