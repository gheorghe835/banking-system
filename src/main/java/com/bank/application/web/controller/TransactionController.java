package com.bank.application.web.controller;

import com.bank.application.web.dto.TransactionDTO;
import com.bank.application.web.webMapper.WebAccountMapper;
import com.bank.application.web.webMapper.WebTransactionMapper;
import com.bank.domain.model.Transaction;
import com.bank.domain.service.TransactionService;
//import com.bank.infrastructure.persistence.mapper.TransactionMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final WebTransactionMapper webTransactionMapper;

    public TransactionController(TransactionService transactionService, WebTransactionMapper webTransactionMapper) {
        this.transactionService = transactionService;
        this.webTransactionMapper = webTransactionMapper;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<TransactionDTO> dtos = transactions.stream()
                .map(webTransactionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getAccountTransactions(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber);
        List<TransactionDTO> dtos = transactions.stream()
                .map(webTransactionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/account/{accountNumber}/last")
    public ResponseEntity<List<TransactionDTO>> getLastTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getLastTransactions(accountNumber, limit);
        List<TransactionDTO> dtos = transactions.stream()
                .map(webTransactionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/account/{accountNumber}/statement")
    public ResponseEntity<List<TransactionDTO>> getAccountStatement(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = transactionService.generateAccountStatement(accountNumber, start, end);
        List<TransactionDTO> dtos = transactions.stream()
                .map(webTransactionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable String transactionId) {
        Transaction transaction = transactionService.findTransaction(transactionId);
        return ResponseEntity.ok(webTransactionMapper.toDto(transaction));
    }
}
