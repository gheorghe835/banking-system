package com.bank.domain.exception;

import com.bank.domain.model.Transaction;

/**
 * Exceptie aruncata cind a tranzactie este invalida
 */

public class InvalidTransactionException extends BankingException{
    private final String transactionId;
    private final Transaction.TransactionType transactionType;

    public InvalidTransactionException(String transactionId,
                                      Transaction.TransactionType transactionType,
                                      String reason){
        super(BankingErrorCode.INVALID_TRANSACTION,
                String.format("Tranzactia %s de tip %s este invalida:%s",
                        transactionId,transactionType,reason));
        this.transactionId = transactionId;
        this.transactionType = transactionType;
    }
    public InvalidTransactionException(String reason){
        super(BankingErrorCode.INVALID_TRANSACTION,reason);
        this.transactionId = null;
        this.transactionType = null;
    }

    //getteri
    public String getTransactionId(){return transactionId;}
    public Transaction.TransactionType getTransactionType(){return transactionType;}

    @Override
    public String toString(){
        if (transactionId != null){
            return String.format("InvalidTransactionException[id=%s, type=%s, message=%s]",
                    transactionId,transactionType,getMessage());
        }
        return String.format("InvalidTransactionException[massage=%s]",getMessage());
    }
}
