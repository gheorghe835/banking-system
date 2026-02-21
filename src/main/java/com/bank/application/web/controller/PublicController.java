package com.bank.application.web.controller;

import com.bank.domain.model.Currency;
import com.bank.domain.service.ExchangeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ExchangeService exchangeService;

    public PublicController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/exchange-rates")
    public Map<String, BigDecimal> getRates() {
        return exchangeService.getAllExchangeRatesAsString();
    }
}