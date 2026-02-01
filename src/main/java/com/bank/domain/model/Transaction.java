package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * clasa care reprezinta o tranzactie bancara
 * inregistreaza toate operatiunile efectuate in sistem
 */

public class Transaction {
    private String transactionId;
    private String sourceAccountNumber; //cont sursa - pentru transfer sau retragere
    private String targetAccountNumber; //cont destinatie - pentru transfer sau depunere
    private TransactionType type;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private LocalDateTime timestamp;
    private TransactionStatus status;

    //tipuri de tranzactii
    public enum TransactionType{
        DEPOSIT, //depunere
        WITHDRAWAL, // retragere
        TRANSFER_OUT, // transfer iesire
        TRANSFER_IN, // transfer intrare
        CURRENCY_EXCHANGE, // schimb valutar
        INTEREST, // dobinda
        FEE, // comision
        ACCOUNT_CREATION, // creare cont
        PASSWORD_CHANGE, // schimbare parola
        ACCOUNT_DEACTIVATED, // dezactivare cont
        ACCOUNT_REACTIVATED // REACTIVARE CONT
    }

    //statusuri
    public enum TransactionStatus{
        PENDING, // in asteptare
        COMPLETED, // finalizata cu succes
        FAILED, // esuata
        CANCELLED // anulata
    }

    //constructori
    public Transaction(){
        this.transactionId = UUID.randomUUID().toString().substring(0,8);
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
    public Transaction(TransactionType type,BigDecimal amount,Currency currency,String description){
        this();
        this.type = Objects.requireNonNull(type,"Tipul tranzactiei este obligatoriu.");
        setAmount(amount);
        this.currency = Objects.requireNonNull(currency,"Moneda este obligatorie.");
        setDescription(description);
    }
    public Transaction(String sourceAccountNumber,String targetAccountNumber,TransactionType type,
                       BigDecimal amount,Currency currency,String description){
        this(type,amount,currency,description);
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
    }

    //validari
    private void setAmount(BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        }
        this.amount = amount;
    }
    private void setDescription(String description){
        this.description = description != null ? description.trim() : "";
        if (this.description.length() > 200){
            this.description = this.description.substring(0,197) + "...";
        }
    }

    //getteri si setteri
    public String getTransactionId(){return transactionId;}
    public String getSourceAccountNumber(){return sourceAccountNumber;}
    public void setSourceAccountNumber(String sourceAccountNumber){
        this.sourceAccountNumber = sourceAccountNumber;
    }
    public String getTargetAccountNumber(){return targetAccountNumber;}
    public void setTargetAccountNumber(String targetAccountNumber){
        this.targetAccountNumber = targetAccountNumber;
    }
    public TransactionType getType(){return type;}
    public void setType(TransactionType type){this.type = type;}
    public BigDecimal getAmount(){return amount;}
    public Currency getCurrency(){return currency;}
    public void setCurrency(Currency currency){this.currency = currency;}
    public String getDescription(){return description;}
    public LocalDateTime getTimestamp(){return timestamp;}
    public TransactionStatus getStatus(){return status;}
    public void setStatus(TransactionStatus status){this.status = status;}
    public void markASCompleted(){this.status = TransactionStatus.COMPLETED;}
    public void markAsFailed(){this.status = TransactionStatus.FAILED;}
    public void markAsCancelled(){this.status = TransactionStatus.CANCELLED;}

    //metode utilitare
    public boolean isSuccessful(){return status == TransactionStatus.COMPLETED;}
    public boolean isFailed(){return status == TransactionStatus.FAILED;}
    public boolean isPending(){return status == TransactionStatus.PENDING;}

    //conversie in MDL
    public BigDecimal getAmountInMDL(){
        if (currency == Currency.MDL){
            return amount;
        }
        return amount.multiply(BigDecimal.valueOf(currency.getExchangeRateToMDL()));
    }

    //override metode
    @Override
    public boolean equals(Object o){
        if (this == o)return true;
        if (o == null || getClass() != o.getClass())return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId,that.transactionId);
    }
    @Override
    public int hashCode(){
        return Objects.hash(transactionId);
    }
    @Override
    public String toString(){
        return String.format("Trancaction[ID =%s, Type =%s, Amount =%s%s, Status =%s, Time =%s]",
                transactionId,
                type,
                amount,
                currency,
                status,
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    //format pentru afisare detaliata
    public String toDetailedString(){
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
            sb.append(String.format("║ Catre: %-31s ║\n", targetAccountNumber));
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


























