package com.p2pfinance.user.api;

import com.p2pfinance.user.dto.AuthenticationResponse;
import com.p2pfinance.user.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @GetMapping("/callback")
    public ResponseEntity<AuthenticationResponse> oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        log.info("OAuth2 callback received with code and state");

        AuthenticationResponse response = oauth2Service.processOAuthCallback(code, state);

        return ResponseEntity.ok(response);
    }
}