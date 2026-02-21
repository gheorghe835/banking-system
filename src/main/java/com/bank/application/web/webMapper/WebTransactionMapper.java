package com.bank.application.web.webMapper;

import com.bank.application.web.dto.TransactionDTO;
import com.bank.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class WebTransactionMapper {

    /**
     * Convertește un obiect Transaction (domain) în TransactionDTO (pentru API)
     */
    public TransactionDTO toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setType(transaction.getType() != null ? transaction.getType().name() : null);
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency() != null ? transaction.getCurrency().getCode() : null);
        dto.setSourceAccount(transaction.getSourceAccountNumber());
        dto.setTargetAccount(transaction.getTargetAccountNumber());
        dto.setDescription(transaction.getDescription());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null);

        return dto;
    }

    /**
     * Convertește un TransactionDTO în Transaction (domain)
     * (folosit pentru a crea tranzacții din cereri API)
     */
    public Transaction toDomain(TransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        // Determină tipul tranzacției
        Transaction.TransactionType type = null;
        if (dto.getType() != null) {
            try {
                type = Transaction.TransactionType.valueOf(dto.getType());
            } catch (IllegalArgumentException e) {
                // Tip invalid - va fi tratat în service
            }
        }

        // Determină moneda
        com.bank.domain.model.Currency currency = null;
        if (dto.getCurrency() != null) {
            try {
                currency = com.bank.domain.model.Currency.fromCode(dto.getCurrency());
            } catch (IllegalArgumentException e) {
                // Monedă invalidă - va fi tratată în service
            }
        }

        // Creează tranzacția
        Transaction transaction = new Transaction(
                type,
                dto.getAmount(),
                currency,
                dto.getDescription()
        );

        transaction.setTransactionId(dto.getTransactionId());
        transaction.setSourceAccountNumber(dto.getSourceAccount());
        transaction.setTargetAccountNumber(dto.getTargetAccount());

        // Statusul se setează implicit ca PENDING în constructor

        return transaction;
    }

    /**
     * Versiune simplificată pentru listări
     */
    public TransactionDTO toSimpleDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setType(transaction.getType() != null ? transaction.getType().name() : null);
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency() != null ? transaction.getCurrency().getCode() : null);
        dto.setDescription(transaction.getDescription());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null);

        // Fără sursă/destinație pentru simplificare

        return dto;
    }

    /**
     * Pentru tranzacții de tip transfer - include detalii despre conturi
     */
    public TransactionDTO toDetailedDto(Transaction transaction) {
        TransactionDTO dto = toDto(transaction);

        return dto;
    }
}