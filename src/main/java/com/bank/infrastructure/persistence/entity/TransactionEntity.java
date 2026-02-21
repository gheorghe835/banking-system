package com.bank.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entitate JPA pentru tranzactii bancare
 */
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @Column(name = "transaction_id", length = 20, nullable = false, unique = true)
    private String transactionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_number", nullable = false)
    private AccountEntity account;

    @Column(name = "transaction_type", length = 30, nullable = false)
    private String transactionType; // "DEPOSIT", "WITHDRAWAL", "TRANSFER_IN", etc.

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency; // "MDL", "EUR", "USD", etc.

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // "PENDING", "COMPLETED", "FAILED", "CANCELLED"

    @Column(name = "source_account", length = 16)
    private String sourceAccount;

    @Column(name = "target_account", length = 16)
    private String targetAccount;

    // Constructor implicit (necesar pentru JPA)
    public TransactionEntity() {
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Constructor cu parametri
    public TransactionEntity(String transactionId, AccountEntity account,
                             String transactionType, BigDecimal amount,
                             String currency, String description) {
        this();
        this.transactionId = transactionId;
        this.account = account;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    // Getters si Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }

    // Metode utilitare
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void markAsFailed() {
        this.status = "FAILED";
    }

    public void markAsCancelled() {
        this.status = "CANCELLED";
    }

    @Override
    public String toString() {
        return String.format("TransactionEntity[id=%s, type=%s, amount=%.2f %s, status=%s, time=%s]",
                transactionId,
                transactionType,
                amount != null ? amount : BigDecimal.ZERO,
                currency,
                status,
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}