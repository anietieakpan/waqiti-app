// File: services/user-service/src/main/java/com/waqiti/user/client/NotificationServiceClient.java
package com.waqiti.user.client;

import com.waqiti.user.dto.TwoFactorNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications/2fa/sms")
    boolean sendTwoFactorSms(@RequestBody TwoFactorNotificationRequest request);

    @PostMapping("/api/v1/notifications/2fa/email")
    boolean sendTwoFactorEmail(@RequestBody TwoFactorNotificationRequest request);
}