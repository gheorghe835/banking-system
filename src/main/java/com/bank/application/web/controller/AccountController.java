package com.bank.application.web.controller;

import com.bank.application.monitoring.MetricsService;
import com.bank.application.web.dto.AccountDTO;
import com.bank.application.web.dto.TransferRequest;
import com.bank.application.web.webMapper.WebAccountMapper;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AccountService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final WebAccountMapper webAccountMapper;
    private final MetricsService metricsService;
    private final MeterRegistry meterRegistry;

    public AccountController(AccountService accountService,
                             WebAccountMapper webAccountMapper,
                             MetricsService metricsService,
                             MeterRegistry meterRegistry) {
        this.accountService = accountService;
        this.webAccountMapper = webAccountMapper;
        this.metricsService = metricsService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountDTO> dtos = accounts.stream()
                .map(webAccountMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.findAccount(accountNumber);
        return ResponseEntity.ok(webAccountMapper.toDto(account));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "MDL") String currency) {
        Currency curr = Currency.fromCode(currency);
        BigDecimal balance = accountService.getBalance(accountNumber, curr);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<?> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "MDL") String currency,
            HttpServletRequest request) {  // ← Adaugă HttpServletRequest pentru detalii

        long startTime = System.currentTimeMillis();

        try {
            Currency curr = Currency.fromCode(currency);
            Account account = accountService.deposit(accountNumber, amount, curr);

            metricsService.incrementTransactionCount();
            metricsService.recordTransactionTime(
                    System.currentTimeMillis() - startTime,
                    TimeUnit.MILLISECONDS
            );
            metricsService.updateTotalBalanceGauge();

            // Înregistrează tipul operației
            meterRegistry.counter("bank.transactions.type",
                    "operation", "deposit",
                    "currency", currency).increment();

            return ResponseEntity.ok(webAccountMapper.toDto(account));

        } catch (InsufficientFundsException e) {
            meterRegistry.counter("bank.transactions.error",
                    "type", "insufficient_funds",
                    "currency", currency).increment();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Fonduri insuficiente: " + e.getMessage()));

        } catch (AccountNotFoundException e) {
            meterRegistry.counter("bank.transactions.error",
                    "type", "account_not_found").increment();

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Cont negăsit: " + e.getMessage()));

        } catch (Exception e) {
            meterRegistry.counter("bank.transactions.error",
                    "type", "other",
                    "error", e.getClass().getSimpleName()).increment();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Eroare internă: " + e.getMessage()));
        }
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountDTO> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "MDL") String currency) {
        Currency curr = Currency.fromCode(currency);
        Account account = accountService.withdraw(accountNumber, amount, curr);
        return ResponseEntity.ok(webAccountMapper.toDto(account));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        Currency currency = Currency.fromCode(request.getCurrency());
        accountService.transfer(
                request.getSourceAccount(),
                request.getTargetAccount(),
                request.getAmount(),
                currency,
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}