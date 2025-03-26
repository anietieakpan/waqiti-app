package com.p2pfinance.payment.client;

import com.p2pfinance.payment.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/v1/users/{userId}")
    UserResponse getUser(@PathVariable UUID userId);
    
    @GetMapping("/api/v1/users")
    List<UserResponse> getUsers(@RequestParam List<UUID> userIds);
}