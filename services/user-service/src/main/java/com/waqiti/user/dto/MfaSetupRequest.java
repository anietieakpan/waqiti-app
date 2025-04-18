// File: services/user-service/src/main/java/com/waqiti/user/dto/MfaSetupRequest.java
package com.waqiti.user.dto;

import com.waqiti.user.domain.MfaMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaSetupRequest {
    @NotNull(message = "MFA method is required")
    private MfaMethod method;

    // For SMS or Email methods
    private String phoneNumber;
    private String email;
}