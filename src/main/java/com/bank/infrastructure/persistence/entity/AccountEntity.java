package com.bank.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entitate JPA pentru conturi bancare
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_number", length = 16, nullable = false, unique = true)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity owner;

    @Column(name = "account_type", length = 20, nullable = false)
    private String accountType; // "CURRENT", "SAVINGS", "BUSINESS"

    @Column(name = "balance_mdl", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceMDL = BigDecimal.ZERO;

    @Column(name = "balance_eur", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceEUR = BigDecimal.ZERO;

    @Column(name = "balance_usd", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceUSD = BigDecimal.ZERO;

    @Column(name = "balance_gbp", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceGBP = BigDecimal.ZERO;

    @Column(name = "balance_ron", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceRON = BigDecimal.ZERO;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "daily_withdrawal_limit", precision = 10, scale = 2, nullable = false)
    private BigDecimal dailyWithdrawalLimit = BigDecimal.valueOf(5000);

    @Column(name = "daily_withdrawal_used", precision = 10, scale = 2, nullable = false)
    private BigDecimal dailyWithdrawalUsed = BigDecimal.ZERO;

    @Column(name = "last_reset_date", nullable = false)
    private LocalDate lastResetDate;

    // Relatie OneToMany cu tranzactiile
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionEntity> transactions = new ArrayList<>();

    // Constructor implicit (necesar pentru JPA)
    public AccountEntity() {
        this.creationDate = LocalDate.now();
        this.lastResetDate = LocalDate.now();
    }

    // Constructor cu parametri
    public AccountEntity(String accountNumber, CustomerEntity owner,
                         String accountType, BigDecimal initialBalanceMDL) {
        this();
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.accountType = accountType;
        this.balanceMDL = initialBalanceMDL != null ? initialBalanceMDL : BigDecimal.ZERO;
    }

    // Getters si Setters
    public String getAccountNumber() {
        return accountNumber;
    }


    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public CustomerEntity getOwner() {
        return owner;
    }

    public void setOwner(CustomerEntity owner) {
        this.owner = owner;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalanceMDL() {
        return balanceMDL;
    }

    public void setBalanceMDL(BigDecimal balanceMDL) {
        this.balanceMDL = balanceMDL;
    }

    public BigDecimal getBalanceEUR() {
        return balanceEUR;
    }

    public void setBalanceEUR(BigDecimal balanceEUR) {
        this.balanceEUR = balanceEUR;
    }

    public BigDecimal getBalanceUSD() {
        return balanceUSD;
    }

    public void setBalanceUSD(BigDecimal balanceUSD) {
        this.balanceUSD = balanceUSD;
    }

    public BigDecimal getBalanceGBP() {
        return balanceGBP;
    }

    public void setBalanceGBP(BigDecimal balanceGBP) {
        this.balanceGBP = balanceGBP;
    }

    public BigDecimal getBalanceRON() {
        return balanceRON;
    }

    public void setBalanceRON(BigDecimal balanceRON) {
        this.balanceRON = balanceRON;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public BigDecimal getDailyWithdrawalUsed() {
        return dailyWithdrawalUsed;
    }

    public void setDailyWithdrawalUsed(BigDecimal dailyWithdrawalUsed) {
        this.dailyWithdrawalUsed = dailyWithdrawalUsed;
    }

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    // Metode utilitare pentru business logic
    public BigDecimal getTotalBalanceInMDL() {
        // Conversia sumelor valutare la MDL folosind rate fixe (pentru exemplu)
        BigDecimal total = balanceMDL;
        total = total.add(balanceEUR.multiply(BigDecimal.valueOf(19.45)));  // EUR -> MDL
        total = total.add(balanceUSD.multiply(BigDecimal.valueOf(17.55)));  // USD -> MDL
        total = total.add(balanceGBP.multiply(BigDecimal.valueOf(22.10)));  // GBP -> MDL
        total = total.add(balanceRON.multiply(BigDecimal.valueOf(3.91)));    // RON -> MDL
        return total;
    }

    public int getAccountAgeInDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(creationDate, LocalDate.now());
    }

    public void resetDailyLimitIfNeeded() {
        if (!LocalDate.now().equals(lastResetDate)) {
            dailyWithdrawalUsed = BigDecimal.ZERO;
            lastResetDate = LocalDate.now();
        }
    }

    @Override
    public String toString() {
        return String.format("AccountEntity[number=%s, owner=%s, type=%s, active=%s, total=%.2f MDL]",
                accountNumber,
                owner != null ? owner.getFullName() : "N/A",
                accountType,
                active,
                getTotalBalanceInMDL());
    }
}