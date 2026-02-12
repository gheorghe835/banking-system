package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, String> {

    // Basic queries
    List<TransactionEntity> findByAccountAccountNumber(String accountNumber);

    // Find by transaction type
    List<TransactionEntity> findByTransactionType(String transactionType);

    // Find by currency
    List<TransactionEntity> findByCurrency(String currency);


    // Find by status
    List<TransactionEntity> findByStatus(String status);

    // Find by amount range
    List<TransactionEntity> findByAmountGreaterThanEqual(BigDecimal minAmount);
    List<TransactionEntity> findByAmountLessThanEqual(BigDecimal maxAmount);
    List<TransactionEntity> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // Find by date range
    List<TransactionEntity> findByTimestampAfter(LocalDateTime startDate);
    List<TransactionEntity> findByTimestampBefore(LocalDateTime endDate);
    List<TransactionEntity> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by source/target account
    List<TransactionEntity> findBySourceAccount(String sourceAccount);
    List<TransactionEntity> findByTargetAccount(String targetAccount);

    // Find transactions between two accounts
    List<TransactionEntity> findBySourceAccountAndTargetAccount(String sourceAccount, String targetAccount);

    // Find by description containing text
    List<TransactionEntity> findByDescriptionContainingIgnoreCase(String text);
    List<TransactionEntity> findByAccountAccountNumberAndTransactionType(
            String accountNumber,
            String transactionType
    );




    // Custom JPQL queries
    @Query("SELECT t FROM TransactionEntity t WHERE t.account.accountNumber = :accountNumber " +
            "AND t.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY t.timestamp DESC")
    List<TransactionEntity> findAccountStatement(@Param("accountNumber") String accountNumber,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT * FROM transactions WHERE account_number = :accountNumber " +
            "ORDER BY timestamp DESC LIMIT :limit",
            nativeQuery = true)
    List<TransactionEntity> findLastTransactions(@Param("accountNumber") String accountNumber,
                                                 @Param("limit") int limit);

    // Statistics queries
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.account.accountNumber = :accountNumber")
    long countByAccount(@Param("accountNumber") String accountNumber);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE " +
            "t.account.accountNumber = :accountNumber AND t.transactionType IN :types")
    BigDecimal sumAmountByAccountAndTypes(@Param("accountNumber") String accountNumber,
                                          @Param("types") List<String> types);

    @Query("SELECT COUNT(t), t.transactionType FROM TransactionEntity t " +
            "WHERE t.account.accountNumber = :accountNumber " +
            "GROUP BY t.transactionType")
    List<Object[]> countTransactionsByType(@Param("accountNumber") String accountNumber);

    // Daily transaction volume
    @Query("SELECT DATE(t.timestamp), SUM(t.amount), COUNT(t) FROM TransactionEntity t " +
            "WHERE t.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(t.timestamp) " +
            "ORDER BY DATE(t.timestamp) DESC")
    List<Object[]> getDailyTransactionVolume(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Find largest transaction
    @Query("SELECT t FROM TransactionEntity t WHERE t.amount = " +
            "(SELECT MAX(t2.amount) FROM TransactionEntity t2 WHERE t2.account.accountNumber = :accountNumber)")
    TransactionEntity findLargestTransactionByAccount(@Param("accountNumber") String accountNumber);

    // Find failed transactions in period
    @Query("SELECT t FROM TransactionEntity t WHERE t.status = 'FAILED' " +
            "AND t.timestamp BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findFailedTransactionsInPeriod(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);
}
