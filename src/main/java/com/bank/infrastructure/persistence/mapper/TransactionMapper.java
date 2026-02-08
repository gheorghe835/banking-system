package com.bank.infrastructure.persistence.mapper;

import com.bank.domain.model.Transaction;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionEntity toEntity(Transaction transaction) {
        if (transaction == null) return null;

        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(transaction.getTransactionId());
        entity.setTransactionType(transaction.getType().name());
        entity.setAmount(transaction.getAmount());
        entity.setCurrency(transaction.getCurrency().getCode());
        entity.setDescription(transaction.getDescription());
        entity.setTimestamp(transaction.getTimestamp());
        entity.setStatus(transaction.getStatus().name());
        entity.setSourceAccount(transaction.getSourceAccountNumber());
        entity.setTargetAccount(transaction.getTargetAccountNumber());

        // AccountEntity va fi setat separat din service
        // entity.setAccount(accountEntity);

        return entity;
    }

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;

        Transaction transaction = new Transaction();
        transaction.setTransactionId(entity.getTransactionId());
        transaction.setType(Transaction.TransactionType.valueOf(entity.getTransactionType()));
        transaction.setAmount(entity.getAmount());
        transaction.setCurrency(com.bank.domain.model.Currency.fromCode(entity.getCurrency()));
        transaction.setDescription(entity.getDescription());
        transaction.setTimestamp(entity.getTimestamp());
        transaction.setStatus(Transaction.TransactionStatus.valueOf(entity.getStatus()));
        transaction.setSourceAccountNumber(entity.getSourceAccount());
        transaction.setTargetAccountNumber(entity.getTargetAccount());

        return transaction;
    }
}
