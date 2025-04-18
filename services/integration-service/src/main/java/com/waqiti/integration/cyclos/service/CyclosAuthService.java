package com.waqiti.integration.cyclos.service;

import com.waqiti.integration.config.CyclosConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyclosAuthService {

    private final CyclosConfig cyclosConfig;

    /**
     * Generates an HTTP Basic Authentication header for Cyclos API
     */
    public String getBasicAuthHeader() {
        String auth = cyclosConfig.getUsername() + ":" + cyclosConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    /**
     * Generates an API Key header for Cyclos API if API Key authentication is enabled
     */
    public String getApiKeyHeader() {
        if (cyclosConfig.isUseApiKey() && cyclosConfig.getApiKey() != null && !cyclosConfig.getApiKey().isEmpty()) {
            return "Api-Key " + cyclosConfig.getApiKey();
        }
        return getBasicAuthHeader();
    }
}