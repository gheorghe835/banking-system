package com.bank.domain.exception;

import com.bank.domain.model.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Excepție aruncată când nu sunt fonduri suficiente pentru o operațiune
 */
public class InsufficientFundsException extends BankingException {

    private final String accountNumber;
    private final BigDecimal requestedAmount;
    private final BigDecimal availableAmount;
    private final Currency currency;

    public InsufficientFundsException(String accountNumber,
                                      BigDecimal requestedAmount,
                                      BigDecimal availableAmount,
                                      Currency currency) {
        super(BankingErrorCode.INSUFFICIENT_FUNDS,
                String.format("Fonduri insuficiente în contul %s. Solicitat: %.2f %s, Disponibil: %.2f %s",
                        accountNumber, requestedAmount, currency, availableAmount, currency));
        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableAmount = availableAmount;
        this.currency = currency;
    }

    // Getteri
    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getShortfall() {
        return requestedAmount.subtract(availableAmount);
    }

    @Override
    public String toString() {
        return String.format("InsufficientFundsException[account=%s, requested=%s %s, available=%s %s]",
                accountNumber,
                requestedAmount.setScale(2, RoundingMode.HALF_UP),
                currency,
                availableAmount.setScale(2, RoundingMode.HALF_UP),
                currency);
    }
}