package com.bank.domain.exception;

/**
 * enum care defineste codurile de eroare pentru sistemul bancar
 * fiecare eroare are un cod unic si un mesal descriptiv
 */

public enum BankingErrorCode {
    //erori generale

    INTERNAL_ERROR("BANK-0001","Eroare interna a sistemului"),
    VALIDATION_ERROR("BANK-0002","Date invalide"),
    NOT_FOUND("BANK-0003","Resursa nu a fost gasita"),

    //erori conturi
    ACCOUNT_NOT_FOUND("ACC-0001","Contul nu exista"),
    ACCOUNT_INACTIVE("ACC-0002","Contul este inactiv"),
    ACCOUNT_BLOCKED("ACC-0003","Contul este blocat"),
    ACCOUNT_ALREADY_EXISTS("ACC-0004","Contul exista deja"),
    INVALID_ACCOUNT_NUMBER("ACC-0005","Numar de cont invalid"),

    //erori tranzactii
    INSUFFICIENT_FUNDS("TRX-0001","Fonduri insuficiente"),
    INVALID_TRANSACTION("TRX-0002","Tranzactie invalida"),
    DAILY_LIMIT_EXCEEDE("TRX-0003","Limita zilnica depasita"),
    TRANSACTION_FAILED("TRX-0004","Tranzactia a esuat"),

    //erori de securitate
    INVALID_CREDENTIALS("SEC-0001","Credentiale invalide"),
    UNAUTHORIZED_ACCESS("SEC-0002","Acces neatorizat"),
    TOO_MANY_ATTEMPTS("SEC-0003","Prea multe incercari esuate"),
    SESSION_EXPIRED("SEC-0004","Sesiunea expirata"),

    //erori schimb valutar
    INVALID_CURRENCY("CUR-0001","Moneda invalida"),
    EXCHANGE_RATE_UNAVAILABLE("CUR-0002","Curs de schimb indisponibil"),
    EXCHANGE_FAILED("CUR-0003","Schimbul valutar a esuat"),

    //erori clienti
    CUSTOMER_NOT_FOUND("CUST-0001","Clientul nu exista"),
    CUSTOMER_INACTIVE("CUST-0002","Clientul este inactiv"),
    DUPLICATE_CUSTOMER("CUST-0003","Clientul exista deja"),

    //erori manager
    MANAGER_NOT_FOUND("MGR-0001","Managerul nu exista"),
    INSUFFICIENT_PRIVILEGES("MGR-0002","Privelegii insuficiente");

    private final String code;
    private final String message;

    BankingErrorCode(String code,String message){
        this.code = code;
        this.message = message;
    }

    public String getCode(){return code;}
    public String getMessage(){return message;}

    @Override
    public String toString(){
        return code + ": " + message;
    }

    /**
     * gaseste un BankingErrorCode dupa cod
     */
    public static BankingErrorCode fromCode(String code){
        for (BankingErrorCode errorCode : values()){
            if (errorCode.getCode().equals(code)){
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Cod de eroare necunoscut: " + code);
    }
}
