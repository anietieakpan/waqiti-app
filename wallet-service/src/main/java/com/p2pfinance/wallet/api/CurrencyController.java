package com.p2pfinance.wallet.api;

import com.p2pfinance.wallet.dto.CurrencyConversionRequest;
import com.p2pfinance.wallet.dto.CurrencyConversionResponse;
import com.p2pfinance.wallet.service.CurrencyConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final CurrencyConversionService currencyConversionService;

    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(
            @Valid @RequestBody CurrencyConversionRequest request) {
        log.info("Currency conversion request received: {} {} to {}",
                request.getAmount(), request.getSourceCurrency(), request.getTargetCurrency());

        BigDecimal exchangeRate = currencyConversionService.getExchangeRate(
                request.getSourceCurrency(), request.getTargetCurrency());

        BigDecimal convertedAmount = currencyConversionService.convert(
                request.getAmount(), request.getSourceCurrency(), request.getTargetCurrency());

        CurrencyConversionResponse response = CurrencyConversionResponse.builder()
                .sourceAmount(request.getAmount())
                .sourceCurrency(request.getSourceCurrency())
                .targetAmount(convertedAmount)
                .targetCurrency(request.getTargetCurrency())
                .exchangeRate(exchangeRate)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate")
    public ResponseEntity<BigDecimal> getExchangeRate(
            @RequestParam String sourceCurrency,
            @RequestParam String targetCurrency) {
        log.info("Exchange rate request received: {} to {}", sourceCurrency, targetCurrency);

        BigDecimal exchangeRate = currencyConversionService.getExchangeRate(
                sourceCurrency, targetCurrency);

        return ResponseEntity.ok(exchangeRate);
    }
}