package com.bank.domain.exception;

import com.bank.domain.model.Currency;

/**
 * exceptie aruncata cind nu sunt fonduri suficiente pentru o operatiune
 */

public class InsufficientFundsException extends BankingException{
    private final String accountNumber;
    private final double requestAmount;
    private final double availableAmount;
    private final Currency currency;

    public InsufficientFundsException(String accountNumber,
                                      double requestAmount,
                                      double availableAmount,
                                      Currency currency){
        super(BankingErrorCode.INSUFFICIENT_FUNDS,
                String.format("Fonduri insuficiente in contul %s. Solicitat: %.2f %s, Disponibil: %.2f%s",
                        accountNumber,requestAmount,currency,availableAmount,currency));
        this.accountNumber = accountNumber;
        this.requestAmount = requestAmount;
        this.availableAmount = availableAmount;
        this.currency = currency;
    }

    //getteri
    public String getAccountNumber(){return accountNumber;}
    public double getRequestedAmount(){return requestAmount;}
    public double getAvailableAmount(){return availableAmount;}
    public Currency getCurrency(){return currency;}
    public double getShortfall(){return requestAmount - availableAmount;}

    @Override
    public String toString(){
        return String.format("InsufficientFundsException[account=%s, requested=%.2f %s, available=%.2f %s]",
                accountNumber,requestAmount,currency,availableAmount,currency);
    }
}
