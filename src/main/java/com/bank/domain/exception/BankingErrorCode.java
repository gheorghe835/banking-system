package com.bank.domain.exception;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum care definește codurile de eroare pentru sistemul bancar
 * Fiecare eroare are un cod unic și un mesaj descriptiv
 */
public enum BankingErrorCode {

    // ===== ERORI GENERALE =====
    INTERNAL_ERROR("BANK-0001", "Eroare internă a sistemului"),
    VALIDATION_ERROR("BANK-0002", "Date invalide"),
    NOT_FOUND("BANK-0003", "Resursa nu a fost găsită"),

    // ===== ERORI CONTURI =====
    ACCOUNT_NOT_FOUND("ACC-0001", "Contul nu există"),
    ACCOUNT_INACTIVE("ACC-0002", "Contul este inactiv"),
    ACCOUNT_BLOCKED("ACC-0003", "Contul este blocat"),
    ACCOUNT_ALREADY_EXISTS("ACC-0004", "Contul există deja"),
    INVALID_ACCOUNT_NUMBER("ACC-0005", "Număr de cont invalid"),

    // ===== ERORI TRANZACȚII =====
    INSUFFICIENT_FUNDS("TRX-0001", "Fonduri insuficiente"),
    INVALID_TRANSACTION("TRX-0002", "Tranzacție invalidă"),
    DAILY_LIMIT_EXCEEDED("TRX-0003", "Limita zilnică depășită"),
    TRANSACTION_FAILED("TRX-0004", "Tranzacția a eșuat"),

    // ===== ERORI SECURITATE =====
    INVALID_CREDENTIALS("SEC-0001", "Credențiale invalide"),
    UNAUTHORIZED_ACCESS("SEC-0002", "Acces neautorizat"),
    TOO_MANY_ATTEMPTS("SEC-0003", "Prea multe încercări eșuate"),
    SESSION_EXPIRED("SEC-0004", "Sesiunea a expirat"),

    // ===== ERORI SCHIMB VALUTAR =====
    INVALID_CURRENCY("CUR-0001", "Monedă invalidă"),
    EXCHANGE_RATE_UNAVAILABLE("CUR-0002", "Curs de schimb indisponibil"),
    EXCHANGE_FAILED("CUR-0003", "Schimbul valutar a eșuat"),

    // ===== ERORI CLIENTI =====
    CUSTOMER_NOT_FOUND("CUST-0001", "Clientul nu există"),
    CUSTOMER_INACTIVE("CUST-0002", "Clientul este inactiv"),
    DUPLICATE_CUSTOMER("CUST-0003", "Clientul există deja"),

    // ===== ERORI MANAGER =====
    MANAGER_NOT_FOUND("MGR-0001", "Managerul nu există"),
    INSUFFICIENT_PRIVILEGES("MGR-0002", "Privilegii insuficiente");

    private final String code;
    private final String message;

    BankingErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }

    /**
     * Găsește un BankingErrorCode după cod
     */
    public static Optional<BankingErrorCode> fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst();
    }
}