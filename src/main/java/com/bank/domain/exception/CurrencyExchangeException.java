package com.bank.domain.exception;

import com.bank.domain.model.Currency;

/**
 * Exceptie aruncata pentru erori la schimb valutar
 */

public class CurrencyExchangeException extends BankingException{
    private final Currency fromCurrency;
    private final Currency toCurrency;
    private final double amount;

    public CurrencyExchangeException(Currency fromCurrency,
                                     Currency toCurrency,
                                     double amount,
                                     String reason){
        super(BankingErrorCode.EXCHANGE_FAILED,
               String.format("Schimb valutar esuat: %.2f%s -> %s. Motiv: %s",
                       amount,fromCurrency,toCurrency,reason) );
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }
    public CurrencyExchangeException(BankingErrorCode errorCode,
                                     Currency fromCurrency,
                                     Currency toCurrency,
                                     double amount,
                                     String reason){
        super(errorCode,
                String.format("Eroare schimb valutar: %.2f %s -> %s. Motiv: %s",
                        amount,fromCurrency,toCurrency,reason));
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }

    //getteri
    public Currency getFromCurrency(){return fromCurrency;}
    public Currency getToCurrency(){return toCurrency;}
    public double getAmount(){return amount;}

    @Override
    public String toString(){
        return String.format("CurrencyExchangeException[%.2f %s -> %s, message=%s",
                amount,fromCurrency,toCurrency,getMessage());
    }

}
