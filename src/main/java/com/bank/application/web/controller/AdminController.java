package com.bank.application.web.controller;

import com.bank.application.web.dto.AccountDTO;
import com.bank.application.web.webMapper.WebAccountMapper;
import com.bank.application.web.webMapper.WebAccountMapper;
import com.bank.domain.model.Account;
import com.bank.domain.model.Customer;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.CustomerService;
import com.bank.domain.service.InterestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final InterestService interestService;
    private final WebAccountMapper webAccountMapper;

    public AdminController(AccountService accountService, CustomerService customerService,
                           InterestService interestService, WebAccountMapper webAccountMapper) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.interestService = interestService;
        this.webAccountMapper = webAccountMapper;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO> createAccount(
            @RequestParam String accountNumber,
            @RequestParam String customerId,
            @RequestParam String accountType,
            @RequestParam BigDecimal initialBalance,
            @RequestParam String password) {

        try {
            Customer customer = customerService.findCustomerById(customerId);

            Account account = accountService.createAccount(
                    accountNumber, customer, accountType, initialBalance, password
            );

            return ResponseEntity.ok(webAccountMapper.toDto(account));

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/accounts/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/accounts/{accountNumber}/block")
    public ResponseEntity<AccountDTO> blockAccount(@PathVariable String accountNumber) {
        Account account = accountService.blockAccount(accountNumber);
        return ResponseEntity.ok(webAccountMapper.toDto(account));
    }

    @PutMapping("/accounts/{accountNumber}/unblock")
    public ResponseEntity<AccountDTO> unblockAccount(@PathVariable String accountNumber) {
        Account account = accountService.unblockAccount(accountNumber);
        return ResponseEntity.ok(webAccountMapper.toDto(account));
    }

    @GetMapping("/reports/balance")
    public ResponseEntity<Map<String, Object>> getBalanceReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalAccounts", accountService.getTotalAccountCount());
        report.put("activeAccounts", accountService.getActiveAccountCount());
        report.put("totalBalance", accountService.getTotalBankBalance());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/inactive-accounts")
    public ResponseEntity<List<AccountDTO>> getInactiveAccounts() {
        List<Account> accounts = accountService.getInactiveAccounts();
        List<AccountDTO> dtos = accounts.stream()
                .map(webAccountMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/interest/apply")
    public ResponseEntity<String> applyInterest() {
        interestService.applyInterestToAllAccounts();
        return ResponseEntity.ok("Dobânda a fost aplicată cu succes");
    }

    @PostMapping("/customers")
    public ResponseEntity<Map<String, String>> createCustomer(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam String identityNumber) {

        Customer customer = customerService.createCustomer(firstName, lastName, email, phone, birthDate, identityNumber);

        Map<String, String> response = new HashMap<>();
        response.put("customerId", customer.getCustomerId());
        response.put("message", "Client creat cu succes");

        return ResponseEntity.ok(response);
    }
}
