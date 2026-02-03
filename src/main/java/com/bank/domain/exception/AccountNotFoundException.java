package com.bank.domain.exception;

/**
 * exceptie aruncata cind un cont bancar nu este gatit
 */

public class AccountNotFoundException extends BankingException{
    private final String accountNumber;

    public AccountNotFoundException(String accountNumber){
        super(BankingErrorCode.ACCOUNT_NOT_FOUND,
                "Contul cu numarul " + accountNumber + ", nu a fost gasit.");
        this.accountNumber = accountNumber;
    }
    public AccountNotFoundException(String accountNumber,Throwable cause){
        super(BankingErrorCode.ACCOUNT_NOT_FOUND,
                "Contul cu numarul " + accountNumber + " nu a fost gasit",cause);
        this.accountNumber = accountNumber;
    }
    public String getAccountNumber(){return accountNumber;}

    @Override
    public String toString(){
        return String.format("AccountNotFoundException[accountNumber=%s, message=%s]",
                accountNumber,getMessage());
    }
}
