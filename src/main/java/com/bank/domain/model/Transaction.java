package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Clasa care reprezintă o tranzacție bancară
 * Înregistrează toate operațiunile efectuate în sistem
 */
public class Transaction {

    private String transactionId;
    private String sourceAccountNumber;     // Cont sursă (pentru transfer/retragere)
    private String targetAccountNumber;     // Cont destinație (pentru transfer/depunere)
    private TransactionType type;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private LocalDateTime timestamp;
    private TransactionStatus status;

    // Tipuri de tranzacții
    public enum TransactionType {
        DEPOSIT,            // Depunere
        WITHDRAWAL,         // Retragere
        TRANSFER_OUT,       // Transfer ieșire
        TRANSFER_IN,        // Transfer intrare
        CURRENCY_EXCHANGE,  // Schimb valutar
        INTEREST,           // Dobândă
        FEE,                // Comision
        ACCOUNT_CREATION,   // Creare cont
        PASSWORD_CHANGE,    // Schimbare parolă
        ACCOUNT_DEACTIVATED,// Dezactivare cont
        ACCOUNT_REACTIVATED // Reactivare cont
    }

    // Statusuri
    public enum TransactionStatus {
        PENDING,    // În așteptare
        COMPLETED,  // Finalizată cu succes
        FAILED,     // Eșuată
        CANCELLED   // Anulată
    }

    // Constructori
    public Transaction() {
        this.transactionId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }

    public Transaction(TransactionType type, BigDecimal amount,
                       Currency currency, String description) {
        this();
        this.type = Objects.requireNonNull(type, "Tipul tranzacției este obligatoriu");

        // Validare directă, fără setter privat
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Suma trebuie să fie pozitivă");
        }
        this.amount = amount;  // ← SETEAZĂ DIRECT!

        this.currency = Objects.requireNonNull(currency, "Moneda este obligatorie");

        // Validare directă
        this.description = description != null ? description.trim() : "";
        if (this.description.length() > 200) {
            this.description = this.description.substring(0, 197) + "...";
        }
    }

    public Transaction(String sourceAccountNumber, String targetAccountNumber,
                       TransactionType type, BigDecimal amount, Currency currency,
                       String description) {
        this(type, amount, currency, description);
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
    }

    // Validări
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Suma trebuie să fie pozitivă");
        }
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
        if (this.description.length() > 200) {
            this.description = this.description.substring(0, 197) + "...";
        }
    }

    // Getteri și Setteri
    public String getTransactionId() {
        return transactionId;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
    }

    public void markAsCancelled() {
        this.status = TransactionStatus.CANCELLED;
    }

    // Metode utilitare
    public boolean isSuccessful() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    // Conversie în MDL
    public BigDecimal getAmountInMDL() {
        if (currency == Currency.MDL) {
            return amount;
        }
        return amount.multiply(BigDecimal.valueOf(currency.getExchangeRateToMDL()));
    }

    // Override metode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return String.format("Transaction[ID=%s, Type=%s, Amount=%s %s, Status=%s, Time=%s]",
                transactionId, type, amount, currency, status,
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    // Format pentru afișare detaliată
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append(String.format("║ TRANZACȚIE ID: %-27s ║\n", transactionId));
        sb.append("╠══════════════════════════════════════════╣\n");
        sb.append(String.format("║ Tip: %-34s ║\n", type));
        sb.append(String.format("║ Suma: %-10.2f %-22s ║\n", amount.floatValue(), currency));

        if (sourceAccountNumber != null) {
            sb.append(String.format("║ De la: %-31s ║\n", sourceAccountNumber));
        }

        if (targetAccountNumber != null) {
            sb.append(String.format("║ Către: %-31s ║\n", targetAccountNumber));
        }

        sb.append(String.format("║ Status: %-32s ║\n", status));
        sb.append(String.format("║ Data: %-33s ║\n",
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));

        if (description != null && !description.isEmpty()) {
            String[] descLines = description.split("(?<=\\G.{30})");
            for (String line : descLines) {
                sb.append(String.format("║ Desc: %-33s ║\n", line));
            }
        }

        sb.append("╚══════════════════════════════════════════╝");
        return sb.toString();
    }
}