package com.bank.application.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class AccountDTO {
    private String accountNumber;
    private String ownerName;
    private String ownerId;
    private String accountType;
    private Map<String, BigDecimal> balances; // "MDL": 1000, "EUR": 500
    private LocalDate creationDate;
    private boolean active;
    private BigDecimal dailyWithdrawalLimit;

    // Constructor gol
    public AccountDTO() {}

    // Constructor cu toate câmpurile
    public AccountDTO(String accountNumber, String ownerName, String ownerId,
                      String accountType, Map<String, BigDecimal> balances,
                      LocalDate creationDate, boolean active,
                      BigDecimal dailyWithdrawalLimit) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.accountType = accountType;
        this.balances = balances;
        this.creationDate = creationDate;
        this.active = active;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    // Getters și Setters pentru toate câmpurile
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Map<String, BigDecimal> getBalances() { return balances; }
    public void setBalances(Map<String, BigDecimal> balances) { this.balances = balances; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getDailyWithdrawalLimit() { return dailyWithdrawalLimit; }
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }
}
