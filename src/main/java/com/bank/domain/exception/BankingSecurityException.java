package com.bank.domain.exception;

/**
 * Excepție aruncată pentru erori de securitate
 */
public class BankingSecurityException extends BankingException {

    private final String username;
    private final String ipAddress;
    private final SecurityAction action;

    // Tipuri de acțiuni de securitate
    public enum SecurityAction {
        LOGIN_ATTEMPT,
        PASSWORD_CHANGE,
        ACCOUNT_ACCESS,
        TRANSACTION_AUTHORIZATION,
        ADMIN_OPERATION
    }

    public BankingSecurityException(BankingErrorCode errorCode,
                                    String username,
                                    SecurityAction action) {
        super(errorCode);
        this.username = username;
        this.action = action;
        this.ipAddress = null;
    }

    public BankingSecurityException(BankingErrorCode errorCode,
                                    String username,
                                    SecurityAction action,
                                    String ipAddress) {
        super(errorCode);
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
    }

    public BankingSecurityException(BankingErrorCode errorCode,
                                    String username,
                                    SecurityAction action,
                                    String details,
                                    String ipAddress) {
        super(errorCode, details);
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
    }

    // Getteri
    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public SecurityAction getAction() {
        return action;
    }

    public boolean isLoginRelated() {
        return action == SecurityAction.LOGIN_ATTEMPT;
    }

    public boolean isAccessRelated() {
        return action == SecurityAction.ACCOUNT_ACCESS ||
                action == SecurityAction.TRANSACTION_AUTHORIZATION;
    }

    @Override
    public String toString() {
        return String.format("BankingSecurityException[user=%s, action=%s, ip=%s, message=%s]",
                username, action, ipAddress != null ? ipAddress : "N/A", getMessage());
    }
}