package com.bank.application.web.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private String sourceAccount;
    private String targetAccount;
    private BigDecimal amount;
    private String currency;
    private String description;

    // Constructor gol
    public TransferRequest() {}

    // Constructor cu parametri
    public TransferRequest(String sourceAccount, String targetAccount,
                           BigDecimal amount, String currency, String description) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    // Getters și Setters
    public String getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }

    public String getTargetAccount() { return targetAccount; }
    public void setTargetAccount(String targetAccount) { this.targetAccount = targetAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}