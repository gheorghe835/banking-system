package com.bank.application.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private String transactionId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private BigDecimal amount;
    private String currency;
    private String sourceAccount;
    private String targetAccount;
    private String description;
    private LocalDateTime timestamp;
    private String status;

    // Constructor gol
    public TransactionDTO() {}

    // Constructor cu parametri
    public TransactionDTO(String transactionId, String type, BigDecimal amount,
                          String currency, String sourceAccount, String targetAccount,
                          String description, LocalDateTime timestamp, String status) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters și Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }

    public String getTargetAccount() { return targetAccount; }
    public void setTargetAccount(String targetAccount) { this.targetAccount = targetAccount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}