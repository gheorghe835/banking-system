package com.bank.domain.repository;

import com.bank.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfață pentru repository-ul de tranzacții bancare
 */
public interface TransactionRepository {

    // ===== CRUD OPERATIONS =====

    /**
     * Salvează o tranzacție
     */
    Transaction save(Transaction transaction);

    /**
     * Găsește o tranzacție după ID
     */
    Optional<Transaction> findById(String transactionId);

    /**
     * Șterge o tranzacție după ID
     */
    boolean deleteById(String transactionId);

    /**
     * Returnează toate tranzacțiile
     */
    List<Transaction> findAll();

    /**
     * Returnează numărul total de tranzacții
     */
    long count();

    // ===== BUSINESS SPECIFIC METHODS =====

    /**
     * Găsește toate tranzacțiile unui cont
     */
    List<Transaction> findByAccountNumber(String accountNumber);

    /**
     * Găsește tranzacțiile unui cont de un anumit tip
     */
    List<Transaction> findByAccountNumberAndType(String accountNumber,
                                                 Transaction.TransactionType transactionType);

    /**
     * Găsește tranzacțiile dintr-o perioadă de timp
     */
    List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Găsește tranzacțiile cu sumă mai mare decât o valoare
     */
    List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount);

    /**
     * Găsește ultimele N tranzacții ale unui cont
     */
    List<Transaction> findLastTransactionsByAccount(String accountNumber, int limit);

    /**
     * Găsește toate transferurile între două conturi
     */
    List<Transaction> findTransfersBetweenAccounts(String sourceAccount, String targetAccount);

    /**
     * Găsește toate tranzacțiile eșuate
     */
    List<Transaction> findFailedTransactions();

    /**
     * Găsește toate tranzacțiile în așteptare
     */
    List<Transaction> findPendingTransactions();

    /**
     * Returnează suma totală a depunerilor pentru un cont
     */
    BigDecimal getTotalDepositsForAccount(String accountNumber);

    /**
     * Returnează suma totală a retragerilor pentru un cont
     */
    BigDecimal getTotalWithdrawalsForAccount(String accountNumber);

    /**
     * Returnează numărul de tranzacții pentru un cont
     */
    long countByAccountNumber(String accountNumber);

    /**
     * Generează un extras de cont pentru o perioadă
     */
    List<Transaction> generateAccountStatement(String accountNumber,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);

    /**
     * Marchează o tranzacție ca finalizată
     */
    boolean markAsCompleted(String transactionId);

    /**
     * Marchează o tranzacție ca eșuată
     */
    boolean markAsFailed(String transactionId);

    /**
     * Anulează o tranzacție
     */
    boolean cancelTransaction(String transactionId);
    BigDecimal getTotalTransactionAmount();
    long countByDate(LocalDate date);
}