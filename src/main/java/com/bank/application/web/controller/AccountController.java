package com.bank.application.web.controller;

import com.bank.application.web.dto.AccountDTO;
import com.bank.application.web.dto.TransferRequest;
import com.bank.application.web.webMapper.WebAccountMapper;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final WebAccountMapper webAccountMapper;

    public AccountController(AccountService accountService, WebAccountMapper webAccountMapper) {
        this.accountService = accountService;
        this.webAccountMapper = webAccountMapper;
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
    public ResponseEntity<AccountDTO> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "MDL") String currency) {
        Currency curr = Currency.fromCode(currency);
        Account account = accountService.deposit(accountNumber, amount, curr);
        return ResponseEntity.ok(webAccountMapper.toDto(account));
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