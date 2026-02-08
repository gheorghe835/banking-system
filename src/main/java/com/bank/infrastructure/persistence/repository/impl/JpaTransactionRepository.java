package com.bank.infrastructure.persistence.repository.impl;

import com.bank.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, String> {

    // ==================== OPERAȚIUNI DE BAZĂ ====================
    Optional<TransactionEntity> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);

    // ==================== QUERY-URI DUPĂ CONT ====================
    List<TransactionEntity> findByAccount_AccountNumber(String accountNumber);
    List<TransactionEntity> findByAccount_Id(Long accountId);

    // Cu paginare pentru ultimele tranzactii
    @Query("SELECT t FROM TransactionEntity t WHERE t.account.accountNumber = :accountNumber ORDER BY t.timestamp DESC")
    List<TransactionEntity> findByAccount_AccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    // ==================== QUERY-URI DUPĂ FILTRE ====================
    List<TransactionEntity> findByAccount_AccountNumberAndTransactionType(String accountNumber, String transactionType);
    List<TransactionEntity> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<TransactionEntity> findByAmountGreaterThanEqual(BigDecimal minAmount);
    List<TransactionEntity> findByStatus(String status);
    List<TransactionEntity> findByCurrency(String currency);
    List<TransactionEntity> findByTransactionType(String transactionType);

    // ==================== QUERY-URI COMPLEXE CU @Query ====================

    // Găsește transferuri între două conturi
    @Query("SELECT t FROM TransactionEntity t WHERE " +
            "(t.sourceAccount = :sourceAccount AND t.targetAccount = :targetAccount) OR " +
            "(t.sourceAccount = :targetAccount AND t.targetAccount = :sourceAccount) " +
            "ORDER BY t.timestamp DESC")
    List<TransactionEntity> findTransfersBetweenAccounts(@Param("sourceAccount") String sourceAccount,
                                                         @Param("targetAccount") String targetAccount);

    // Găsește tranzacții după status (specializate)
    @Query("SELECT t FROM TransactionEntity t WHERE t.status IN ('FAILED', 'CANCELLED')")
    List<TransactionEntity> findFailedTransactions();

    @Query("SELECT t FROM TransactionEntity t WHERE t.status = 'PENDING'")
    List<TransactionEntity> findPendingTransactions();

    @Query("SELECT t FROM TransactionEntity t WHERE t.status = 'COMPLETED'")
    List<TransactionEntity> findCompletedTransactions();

    // Găsește tranzacții după descriere (căutare parțială case-insensitive)
    @Query("SELECT t FROM TransactionEntity t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :descriptionPart, '%'))")
    List<TransactionEntity> findByDescriptionContaining(@Param("descriptionPart") String descriptionPart);

    // Găsește tranzacții după cont sursă sau destinație
    @Query("SELECT t FROM TransactionEntity t WHERE " +
            "t.sourceAccount = :accountNumber OR t.targetAccount = :accountNumber " +
            "ORDER BY t.timestamp DESC")
    List<TransactionEntity> findBySourceOrTargetAccount(@Param("accountNumber") String accountNumber);

    // ==================== CALCULE ȘI AGREGĂRI ====================

    // Calculează suma totală a depunerilor pentru un cont
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE " +
            "t.account.accountNumber = :accountNumber AND " +
            "t.transactionType IN ('DEPOSIT', 'TRANSFER_IN', 'INTEREST') AND " +
            "t.status = 'COMPLETED'")
    BigDecimal getTotalDepositsForAccount(@Param("accountNumber") String accountNumber);

    // Calculează suma totală a retragerilor pentru un cont
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE " +
            "t.account.accountNumber = :accountNumber AND " +
            "t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT', 'EXCHANGE', 'FEE') AND " +
            "t.status = 'COMPLETED'")
    BigDecimal getTotalWithdrawalsForAccount(@Param("accountNumber") String accountNumber);

    // Numără tranzacțiile pentru un cont
    long countByAccount_AccountNumber(String accountNumber);

    // Calculează suma totală a tuturor tranzacțiilor
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalTransactionAmount();

    // Generează extras de cont pentru o perioadă
    @Query("SELECT t FROM TransactionEntity t WHERE " +
            "(t.account.accountNumber = :accountNumber OR t.sourceAccount = :accountNumber OR t.targetAccount = :accountNumber) AND " +
            "t.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY t.timestamp DESC")
    List<TransactionEntity> generateAccountStatement(@Param("accountNumber") String accountNumber,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // Calculează fluxul net pentru un cont
    @Query("SELECT " +
            "(SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE " +
            "t.account.accountNumber = :accountNumber AND " +
            "t.transactionType IN ('DEPOSIT', 'TRANSFER_IN', 'INTEREST') AND " +
            "t.status = 'COMPLETED') - " +
            "(SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE " +
            "t.account.accountNumber = :accountNumber AND " +
            "t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT', 'EXCHANGE', 'FEE') AND " +
            "t.status = 'COMPLETED')")
    BigDecimal getNetCashFlow(@Param("accountNumber") String accountNumber);

    // ==================== OPERAȚIUNI DE MODIFICARE ====================

    @Transactional
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = :status WHERE t.transactionId = :transactionId")
    int updateTransactionStatus(@Param("transactionId") String transactionId,
                                @Param("status") String status);

    @Transactional
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = 'COMPLETED' WHERE t.transactionId = :transactionId")
    int markAsCompleted(@Param("transactionId") String transactionId);

    @Transactional
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = 'FAILED' WHERE t.transactionId = :transactionId")
    int markAsFailed(@Param("transactionId") String transactionId);

    @Transactional
    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = 'CANCELLED' WHERE t.transactionId = :transactionId")
    int markAsCancelled(@Param("transactionId") String transactionId);

    // ==================== QUERY-URI PENTRU RAPOARTE ȘI STATISTICI ====================

    // Găsește tranzacții recente
    @Query("SELECT t FROM TransactionEntity t WHERE t.timestamp >= :sinceDate ORDER BY t.timestamp DESC")
    List<TransactionEntity> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate);

    // Șterge tranzacții vechi (pentru cleanup)
    @Transactional
    @Modifying
    @Query("DELETE FROM TransactionEntity t WHERE t.timestamp < :olderThan")
    int deleteOldTransactions(@Param("olderThan") LocalDateTime olderThan);

    // Statistici zilnice
    @Query("SELECT DATE(t.timestamp) as transactionDate, COUNT(*) as count, SUM(t.amount) as total " +
            "FROM TransactionEntity t " +
            "WHERE t.timestamp BETWEEN :startDate AND :endDate AND t.status = 'COMPLETED' " +
            "GROUP BY DATE(t.timestamp) " +
            "ORDER BY transactionDate DESC")
    List<Object[]> getDailyTransactionStats(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}
