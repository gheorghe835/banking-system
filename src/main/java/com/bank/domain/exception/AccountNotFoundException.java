package com.bank.domain.exception;

/**
 * Excepție aruncată când un cont bancar nu este găsit
 */
public class AccountNotFoundException extends BankingException {

    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super(BankingErrorCode.ACCOUNT_NOT_FOUND,
                "Contul cu numărul '" + accountNumber + "' nu a fost găsit");
        this.accountNumber = accountNumber;
    }

    public AccountNotFoundException(String accountNumber, Throwable cause) {
        super(BankingErrorCode.ACCOUNT_NOT_FOUND,
                "Contul cu numărul '" + accountNumber + "' nu a fost găsit",
                cause);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String toString() {
        return String.format("AccountNotFoundException[accountNumber=%s, message=%s]",
                accountNumber, getMessage());
    }
}
