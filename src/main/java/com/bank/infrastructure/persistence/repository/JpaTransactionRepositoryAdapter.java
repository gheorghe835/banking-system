package com.bank.infrastructure.persistence.repository;

import com.bank.domain.model.Transaction;
import com.bank.domain.repository.TransactionRepository;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.TransactionEntity;
import com.bank.infrastructure.persistence.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaTransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;
    private final JpaAccountRepository jpaAccountRepository;  // ← ADUGĂ ASTA
    private final TransactionMapper transactionMapper;

    @Autowired
    public JpaTransactionRepositoryAdapter(JpaTransactionRepository jpaTransactionRepository,
                                           JpaAccountRepository jpaAccountRepository,
                                           TransactionMapper transactionMapper) {
        this.jpaTransactionRepository = jpaTransactionRepository;
        this.jpaAccountRepository = jpaAccountRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionMapper.toEntity(transaction);

        if (transaction.getTargetAccountNumber() != null) {
            AccountEntity accountEntity = jpaAccountRepository
                    .findById(transaction.getTargetAccountNumber())
                    .orElse(null);
            entity.setAccount(accountEntity);
        } else if (transaction.getSourceAccountNumber() != null) {
            AccountEntity accountEntity = jpaAccountRepository
                    .findById(transaction.getSourceAccountNumber())
                    .orElse(null);
            entity.setAccount(accountEntity);
        }

        TransactionEntity savedEntity = jpaTransactionRepository.save(entity);
        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return jpaTransactionRepository.findById(transactionId)
                .map(transactionMapper::toDomain);
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
    public List<Transaction> findAll() {
        return jpaTransactionRepository.findAll().stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaTransactionRepository.count();
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return jpaTransactionRepository.findByAccountAccountNumber(accountNumber).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }



    @Override
    public List<Transaction> findByAccountNumberAndType(String accountNumber,
                             Transaction.TransactionType transactionType) {
        return jpaTransactionRepository.findByAccountAccountNumberAndTransactionType(
                        accountNumber, transactionType.name()).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaTransactionRepository.findByTimestampBetween(startDate, endDate).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount) {
        return jpaTransactionRepository.findByAmountGreaterThanEqual(minAmount).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findLastTransactionsByAccount(String accountNumber, int limit) {
        return jpaTransactionRepository.findLastTransactions(accountNumber, limit).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findTransfersBetweenAccounts(String sourceAccount, String targetAccount) {
        return jpaTransactionRepository.findBySourceAccountAndTargetAccount(sourceAccount, targetAccount).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findFailedTransactions() {
        return jpaTransactionRepository.findByStatus("FAILED").stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findPendingTransactions() {
        return jpaTransactionRepository.findByStatus("PENDING").stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalDepositsForAccount(String accountNumber) {
        List<String> depositTypes = List.of("DEPOSIT", "TRANSFER_IN");
        BigDecimal sum = jpaTransactionRepository.sumAmountByAccountAndTypes(accountNumber, depositTypes);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalWithdrawalsForAccount(String accountNumber) {
        List<String> withdrawalTypes = List.of("WITHDRAWAL", "TRANSFER_OUT");
        BigDecimal sum = jpaTransactionRepository.sumAmountByAccountAndTypes(accountNumber, withdrawalTypes);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public long countByAccountNumber(String accountNumber) {
        return jpaTransactionRepository.countByAccount(accountNumber);
    }

    @Override
    public List<Transaction> generateAccountStatement(String accountNumber,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        return jpaTransactionRepository.findAccountStatement(accountNumber, startDate, endDate).stream()
                .map(transactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markAsCompleted(String transactionId) {
        return findById(transactionId)
                .map(transaction -> {
                    transaction.markAsCompleted();
                    save(transaction);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean markAsFailed(String transactionId) {
        return findById(transactionId)
                .map(transaction -> {
                    transaction.markAsFailed();
                    save(transaction);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean cancelTransaction(String transactionId) {
        return findById(transactionId)
                .map(transaction -> {
                    transaction.markAsCancelled();
                    save(transaction);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public BigDecimal getTotalTransactionAmount() {
        // Implementează după nevoie
        return BigDecimal.ZERO;
    }

    @Override
    public long countByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return jpaTransactionRepository.countByTimestampBetween(startOfDay, endOfDay);
    }
}