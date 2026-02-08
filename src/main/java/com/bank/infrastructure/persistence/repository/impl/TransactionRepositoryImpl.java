package com.bank.infrastructure.persistence.repository.impl;

import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.TransactionRepository;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.TransactionEntity;
import com.bank.infrastructure.persistence.repository.JpaAccountRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;
    private final JpaAccountRepository jpaAccountRepository;

    public TransactionRepositoryImpl(JpaTransactionRepository jpaTransactionRepository,
                                     JpaAccountRepository jpaAccountRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
        this.jpaAccountRepository = jpaAccountRepository;
    }

    // ==================== METODE CRUD DE BAZĂ ====================

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity saved = jpaTransactionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return jpaTransactionRepository.findByTransactionId(transactionId)
                .map(this::toDomain);
    }

    @Override
    public boolean deleteById(String transactionId) {
        if (jpaTransactionRepository.existsById(transactionId)) {
            jpaTransactionRepository.deleteById(transactionId);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(String transactionId) {
        return jpaTransactionRepository.existsById(transactionId);
    }

    @Override
    public List<Transaction> findAll() {
        return jpaTransactionRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaTransactionRepository.count();
    }

    // ==================== METODE SPECIFICE BUSINESS ====================

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return jpaTransactionRepository.findByAccount_AccountNumber(accountNumber).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountNumberAndType(String accountNumber, Transaction.TransactionType transactionType) {
        return jpaTransactionRepository.findByAccount_AccountNumberAndTransactionType(
                        accountNumber,
                        transactionType.name()
                ).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaTransactionRepository.findByTimestampBetween(startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAmountGreaterThanEqual(double minAmount) {
        return jpaTransactionRepository.findByAmountGreaterThanEqual(BigDecimal.valueOf(minAmount)).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findLastTransactionByAccount(String accountNumber, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        List<TransactionEntity> entities = jpaTransactionRepository
                .findByAccount_AccountNumber(accountNumber, pageable);

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findTransfersBetweenAccounts(String sourceAccount, String targetAccount) {
        return jpaTransactionRepository.findTransfersBetweenAccounts(sourceAccount, targetAccount).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findFailedTransaction() {
        return jpaTransactionRepository.findFailedTransactions().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findPendingTransaction() {
        return jpaTransactionRepository.findPendingTransactions().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findCompletedTransaction() {
        return jpaTransactionRepository.findCompletedTransactions().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalDepositsForAccount(String accountNumber) {
        BigDecimal total = jpaTransactionRepository.getTotalDepositsForAccount(accountNumber);
        return total != null ? total.doubleValue() : 0.0;
    }

    ///@Override
    //public double getTotalWithdrawalsForAccount(String accountNumber) {
    //    return 0;
    //}

    @Override
    public double getTotalWithdrawalsForAccount(String accountNumber) {
        BigDecimal total = jpaTransactionRepository.getTotalWithdrawalsForAccount(accountNumber);
        return total != null ? total.doubleValue() : 0.0;
    }

    @Override
    public long countByAccountNumber(String accountNumber) {
        return jpaTransactionRepository.countByAccount_AccountNumber(accountNumber);
    }

    @Override
    public List<Transaction> generateAccountStatement(String accountNumber, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaTransactionRepository.generateAccountStatement(accountNumber, startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalTransactionAmount() {
        BigDecimal total = jpaTransactionRepository.getTotalTransactionAmount();
        return total != null ? total.doubleValue() : 0.0;
    }

    @Override
    public List<Transaction> findByCurrency(String currency) {
        return jpaTransactionRepository.findByCurrency(currency).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markAsCompleted(String transactionId) {
        int updated = jpaTransactionRepository.markAsCompleted(transactionId);
        return updated > 0;
    }

    @Override
    public boolean markAsFailed(String transactionId) {
        int updated = jpaTransactionRepository.markAsFailed(transactionId);
        return updated > 0;
    }

    @Override
    public boolean markAsCancelled(String transactionId) {
        int updated = jpaTransactionRepository.markAsCancelled(transactionId);
        return updated > 0;
    }

    @Override
    public List<Transaction> findByDescriptionContaining(String descriptionPart) {
        return jpaTransactionRepository.findByDescriptionContaining(descriptionPart).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByType(Transaction.TransactionType type) {
        return jpaTransactionRepository.findByTransactionType(type.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ==================== METODE DE CONVERSIE (ADAUGATE) ====================

    /**
     * Convertește un obiect Transaction (domain) în TransactionEntity (JPA)
     */
    private TransactionEntity toEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(transaction.getTransactionId());
        entity.setSourceAccount(transaction.getSourceAccountNumber());
        entity.setTargetAccount(transaction.getTargetAccountNumber());
        entity.setTransactionType(transaction.getType().name());
        entity.setAmount(transaction.getAmount()); // Ambele sunt BigDecimal
        entity.setCurrency(transaction.getCurrency().getCode());
        entity.setDescription(transaction.getDescription());
        entity.setTimestamp(transaction.getTimestamp());
        entity.setStatus(transaction.getStatus().name());

        // Asociază entitatea AccountEntity dacă există
        if (transaction.getSourceAccountNumber() != null && !transaction.getSourceAccountNumber().isEmpty()) {
            Optional<AccountEntity> accountOpt = jpaAccountRepository.findByAccountNumber(transaction.getSourceAccountNumber());
            accountOpt.ifPresent(entity::setAccount);
        } else if (transaction.getTargetAccountNumber() != null && !transaction.getTargetAccountNumber().isEmpty()) {
            Optional<AccountEntity> accountOpt = jpaAccountRepository.findByAccountNumber(transaction.getTargetAccountNumber());
            accountOpt.ifPresent(entity::setAccount);
        }

        return entity;
    }

    /**
     * Convertește un obiect TransactionEntity (JPA) în Transaction (domain)
     */
    private Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            return new Transaction(
                    entity.getTransactionId(),
                    entity.getSourceAccount(),
                    entity.getTargetAccount(),
                    Transaction.TransactionType.valueOf(entity.getTransactionType()),
                    entity.getAmount(),
                    Currency.fromCode(entity.getCurrency()),
                    entity.getDescription(),
                    entity.getTimestamp(),
                    Transaction.TransactionStatus.valueOf(entity.getStatus())
            );
        } catch (IllegalArgumentException e) {
            // Fallback pentru valori invalide
            return new Transaction(
                    entity.getTransactionId(),
                    entity.getSourceAccount(),
                    entity.getTargetAccount(),
                    Transaction.TransactionType.DEPOSIT,
                    entity.getAmount() != null ? entity.getAmount() : BigDecimal.ZERO,
                    Currency.MDL,
                    entity.getDescription(),
                    entity.getTimestamp(),
                    Transaction.TransactionStatus.PENDING
            );
        }
    }
}