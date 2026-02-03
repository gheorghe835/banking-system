package com.bank.domain.exception;

/**
 * exceptie de baza pentru toate erorile din sistemul bancar
 * extinde RuntimeException pentru a fi unchecked exception
 */

public class BankingException extends RuntimeException{

    private final BankingErrorCode errorCode;
    private final String details;

    /**
     * constructor cu cod de eroare
     */
    public BankingException(BankingErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * constructor cu cod dr eroare si mesaj detaliat
     */
    public BankingException(BankingErrorCode errorCode,String details){
        super(details != null ? errorCode.getMessage() + ": " + details : errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * constructor cu cod de eroare, mesaj detaliat si cauza
     */
    public BankingException(BankingErrorCode errorCode,String details,Throwable cause){
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * constructor cu cod de eroare si cauza
     */
    public BankingException(BankingErrorCode errorCode,Throwable cause){
        super(errorCode.getMessage(),cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    //getteri

    public BankingErrorCode getErrorCode(){return errorCode;}
    public String getDetails(){return details;}
    public String getFullMessage(){
        if (details != null && !details.isEmpty()){
            return String.format("[%s] %s: %s",errorCode.getCode(),errorCode.getMessage(),details);
        }
        return String.format("[%s]%s",errorCode.getCode(),errorCode.getMessage());
    }

    //metode utilitare pentru verificare rapida a tipului de eroare
    public boolean isAccountNotFound(){
        return errorCode == BankingErrorCode.ACCOUNT_NOT_FOUND;
    }
    public boolean isInsufficientFunds(){
        return errorCode == BankingErrorCode.INSUFFICIENT_FUNDS;
    }
    public boolean isSecurityError(){
        return errorCode.getCode().startsWith("SEC-");
    }
    public boolean isTransactionError(){
        return errorCode.getCode().startsWith("SEC-");
    }

    @Override
    public String toString(){
        return getFullMessage();
    }
}
