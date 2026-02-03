package com.bank.domain.exception;

/**
 * Exceptie aruncata pentru erori de securitate
 */

public class SecurityException extends BankingException{
    private final String username;
    private final String ipAddress;
    private final SecurityAction action;

    //Tipuri de actiuni de securitate
    public enum SecurityAction{
        LOGIN_ATTEMPT,
        PASSWORD_CHANGE,
        ACCOUNT_ACCESS,
        TRANSACTION_AUTHORIZATION,
        ADMIN_OPERATION
    }

    public SecurityException(BankingErrorCode errorCode,
                             String username,
                             SecurityAction action){
        super(errorCode);
        this.username = username;
        this.action = action;
        this.ipAddress = null;
    }
    public SecurityException(BankingErrorCode errorCode,
                          String username,
                          SecurityAction action,
                          String ipAddress){
        super(errorCode);
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
    }
    public SecurityException(BankingErrorCode errorCode,
                             String username,
                             SecurityAction action,
                             String details,
                             String ipAddress){
        super(errorCode,details);
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
    }

    //getteri
    public String getUsername(){return username;}
    public String getIpAddress(){return ipAddress;}
    public SecurityAction getAction(){return action;}
    public boolean isLoginRelated(){return action == SecurityAction.LOGIN_ATTEMPT;}
    public boolean isAccessRelated(){return action == SecurityAction.ACCOUNT_ACCESS ||
                                            action == SecurityAction.TRANSACTION_AUTHORIZATION;}


    @Override
    public String toString(){
        return String.format("SecurityException[user=%s, action=%s, ip=%s, message=%s]",
                username,action,ipAddress != null ? ipAddress : "N/A",getMessage());
    }
}
