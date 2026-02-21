package com.bank.domain.exception;

/**
 * Excepție de bază pentru toate erorile din sistemul bancar
 * Extinde RuntimeException pentru a fi unchecked exception
 */
public class BankingException extends RuntimeException {

    private final BankingErrorCode errorCode;
    private final String details;

    /**
     * Constructor cu cod de eroare
     */
    public BankingException(BankingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Constructor cu cod de eroare și mesaj detaliat
     */
    public BankingException(BankingErrorCode errorCode, String details) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Constructor cu cod de eroare, mesaj detaliat și cauză
     */
    public BankingException(BankingErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""), cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Constructor cu cod de eroare și cauză
     */
    public BankingException(BankingErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    // Getteri
    public BankingErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }

    public String getFullMessage() {
        if (details != null && !details.isEmpty()) {
            return String.format("[%s] %s: %s",
                    errorCode.getCode(), errorCode.getMessage(), details);
        }
        return String.format("[%s] %s", errorCode.getCode(), errorCode.getMessage());
    }

    // Metode utilitare pentru verificare rapidă a tipului de eroare
    public boolean isAccountNotFound() {
        return errorCode == BankingErrorCode.ACCOUNT_NOT_FOUND;
    }

    public boolean isInsufficientFunds() {
        return errorCode == BankingErrorCode.INSUFFICIENT_FUNDS;
    }

    public boolean isSecurityError() {
        return errorCode.getCode().startsWith("SEC-");
    }

    public boolean isTransactionError() {
        return errorCode.getCode().startsWith("TRX-");
    }

    @Override
    public String toString() {
        return getFullMessage();
    }
}
