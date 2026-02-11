package com.bank.domain.repository;

import com.bank.domain.model.Transaction;

import java.math.BigDecimal;
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
     * @param transaction Tranzacția de salvat
     * @return Tranzacția salvată
     */
    Transaction save(Transaction transaction);

    /**
     * Găsește o tranzacție după ID
     * @param transactionId ID-ul tranzacției
     * @return Optional care conține tranzacția dacă este găsită
     */
    Optional<Transaction> findById(String transactionId);

    /**
     * Șterge o tranzacție după ID
     * @param transactionId ID-ul tranzacției de șters
     * @return true dacă tranzacția a fost ștearsă
     */
    boolean deleteById(String transactionId);

    /**
     * Returnează toate tranzacțiile
     * @return Lista tuturor tranzacțiilor
     */
    List<Transaction> findAll();

    /**
     * Returnează numărul total de tranzacții
     * @return Numărul de tranzacții
     */
    long count();

    // ===== BUSINESS SPECIFIC METHODS =====

    /**
     * Găsește toate tranzacțiile unui cont
     * @param accountNumber Numărul contului
     * @return Lista tranzacțiilor contului
     */
    List<Transaction> findByAccountNumber(String accountNumber);

    /**
     * Găsește tranzacțiile unui cont de un anumit tip
     * @param accountNumber Numărul contului
     * @param transactionType Tipul tranzacției
     * @return Lista tranzacțiilor filtrate
     */
    List<Transaction> findByAccountNumberAndType(String accountNumber,
                                                 Transaction.TransactionType transactionType);

    /**
     * Găsește tranzacțiile dintr-o perioadă de timp
     * @param startDate Data de început
     * @param endDate Data de sfârșit
     * @return Lista tranzacțiilor din perioada specificată
     */
    List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Găsește tranzacțiile cu sumă mai mare decât o valoare
     * @param minAmount Suma minimă
     * @return Lista tranzacțiilor cu sumă >= minAmount
     */
    List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount);

    /**
     * Găsește ultimele N tranzacții ale unui cont
     * @param accountNumber Numărul contului
     * @param limit Numărul maxim de tranzacții
     * @return Lista ultimelor N tranzacții
     */
    List<Transaction> findLastTransactionsByAccount(String accountNumber, int limit);

    /**
     * Găsește toate transferurile între două conturi
     * @param sourceAccount Contul sursă
     * @param targetAccount Contul destinație
     * @return Lista transferurilor între conturi
     */
    List<Transaction> findTransfersBetweenAccounts(String sourceAccount, String targetAccount);

    /**
     * Găsește toate tranzacțiile eșuate
     * @return Lista tranzacțiilor eșuate
     */
    List<Transaction> findFailedTransactions();

    /**
     * Găsește toate tranzacțiile în așteptare
     * @return Lista tranzacțiilor în așteptare
     */
    List<Transaction> findPendingTransactions();

    /**
     * Returnează suma totală a depunerilor pentru un cont
     * @param accountNumber Numărul contului
     * @return Suma totală depusă
     */
    BigDecimal getTotalDepositsForAccount(String accountNumber);

    /**
     * Returnează suma totală a retragerilor pentru un cont
     * @param accountNumber Numărul contului
     * @return Suma totală retrasă
     */
    BigDecimal getTotalWithdrawalsForAccount(String accountNumber);

    /**
     * Returnează numărul de tranzacții pentru un cont
     * @param accountNumber Numărul contului
     * @return Numărul de tranzacții
     */
    long countByAccountNumber(String accountNumber);

    /**
     * Generează un extras de cont pentru o perioadă
     * @param accountNumber Numărul contului
     * @param startDate Data de început
     * @param endDate Data de sfârșit
     * @return Lista tranzacțiilor din perioadă
     */
    List<Transaction> generateAccountStatement(String accountNumber,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);

    /**
     * Marchează o tranzacție ca finalizată
     * @param transactionId ID-ul tranzacției
     * @return true dacă a fost marcată cu succes
     */
    boolean markAsCompleted(String transactionId);

    /**
     * Marchează o tranzacție ca eșuată
     * @param transactionId ID-ul tranzacției
     * @return true dacă a fost marcată cu succes
     */
    boolean markAsFailed(String transactionId);

    /**
     * Anulează o tranzacție
     * @param transactionId ID-ul tranzacției
     * @return true dacă a fost anulată cu succes
     */
    boolean cancelTransaction(String transactionId);
    BigDecimal getTotalTransactionAmount();
}