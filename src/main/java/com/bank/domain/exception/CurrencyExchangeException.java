package com.bank.domain.exception;

import com.bank.domain.model.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Excepție aruncată pentru erori la schimb valutar
 */
public class CurrencyExchangeException extends BankingException {

    private final Currency fromCurrency;
    private final Currency toCurrency;
    private final BigDecimal amount;

    public CurrencyExchangeException(Currency fromCurrency,
                                     Currency toCurrency,
                                     BigDecimal amount,
                                     String reason) {
        super(BankingErrorCode.EXCHANGE_FAILED,
                String.format("Schimb valutar esuat: %.2f %s -> %s. Motiv: %s",
                        amount, fromCurrency, toCurrency, reason));
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }

    public CurrencyExchangeException(BankingErrorCode errorCode,
                                     Currency fromCurrency,
                                     Currency toCurrency,
                                     BigDecimal amount,
                                     String reason) {
        super(errorCode,
                String.format("Eroare schimb valutar: %.2f %s -> %s. Motiv: %s",
                        amount, fromCurrency, toCurrency, reason));
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }

    // Getteri
    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }


    @Override
    public String toString() {
        return String.format("CurrencyExchangeException[%.2f %s->%s, message=%s]",
                amount.setScale(2,RoundingMode.HALF_UP),
                fromCurrency,
                toCurrency,
                getMessage());
    }


}