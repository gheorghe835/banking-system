package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.*;
import com.bank.domain.model.Currency;
import com.bank.domain.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviciu pentru gestionarea tranzacțiilor bancare
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    // ===== OPERAȚIUNI DE BAZĂ PE TRANZACȚII =====

    /**
     * Găsește o tranzacție după ID
     */
    public Transaction findTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BankingException(BankingErrorCode.NOT_FOUND,
                        "Tranzacția cu ID-ul " + transactionId + " nu a fost găsită"));
    }

    /**
     * Returnează toate tranzacțiile
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Returnează tranzacțiile unui cont
     */
    public List<Transaction> getAccountTransactions(String accountNumber) {
        return transactionRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Returnează ultimele N tranzacții ale unui cont
     */
    public List<Transaction> getLastTransactions(String accountNumber, int limit) {
        if (limit <= 0 || limit > 100) {
            throw new ValidationException("Limită invalidă")
                    .addError("limit", "Limită trebuie să fie între 1 și 100", limit);
        }

        return transactionRepository.findLastTransactionsByAccount(accountNumber, limit);
    }

    // ===== RAPOARTE ȘI ISTORIC =====

    /**
     * Generează extras de cont pentru o perioadă
     */
    public List<Transaction> generateAccountStatement(String accountNumber,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        // Validare date
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Perioadă invalidă")
                    .addError("startDate", "Data de început trebuie să fie înainte de data de sfârșit", startDate)
                    .addError("endDate", "Data de sfârșit trebuie să fie după data de început", endDate);
        }

        // Verifică dacă contul există
        accountService.findAccount(accountNumber);

        return transactionRepository.generateAccountStatement(accountNumber, startDate, endDate);
    }

    /**
     * Returnează tranzacțiile dintr-o perioadă
     */
    public List<Transaction> getTransactionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTimestampBetween(startDate, endDate);
    }

    /**
     * Returnează tranzacțiile de un anumit tip
     */
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {

        List<Transaction> allTransactions = transactionRepository.findAll();
        return allTransactions.stream()
                .filter(t -> t.getType() == type)
                .toList();
    }

    /**
     * Returnează suma totală depusă într-un cont
     */
    public BigDecimal getTotalDeposits(String accountNumber) {
        return transactionRepository.getTotalDepositsForAccount(accountNumber);
    }

    /**
     * Returnează suma totală retrasă dintr-un cont
     */
    public BigDecimal getTotalWithdrawals(String accountNumber) {
        return transactionRepository.getTotalWithdrawalsForAccount(accountNumber);
    }

    /**
     * Calculează fluxul de numerar pentru un cont
     */
    public BigDecimal getNetCashFlow(String accountNumber) {
        BigDecimal deposits = getTotalDeposits(accountNumber);
        BigDecimal withdrawals = getTotalWithdrawals(accountNumber);
        return deposits.subtract(withdrawals);
    }

    // ===== STATISTICI =====

    /**
     * Returnează numărul total de tranzacții
     */
    public long getTotalTransactionCount() {
        return transactionRepository.count();
    }

    /**
     * Returnează numărul de tranzacții pentru un cont
     */
    public long getTransactionCountForAccount(String accountNumber) {
        return transactionRepository.countByAccountNumber(accountNumber);
    }

    /**
     * Returnează tranzacțiile eșuate
     */
    public List<Transaction> getFailedTransactions() {
        return transactionRepository.findFailedTransactions();
    }

    /**
     * Returnează tranzacțiile în așteptare
     */
    public List<Transaction> getPendingTransactions() {
        return transactionRepository.findPendingTransactions();
    }

    /**
     * Returnează suma totală a tuturor tranzacțiilor
     */
    public BigDecimal getTotalTransactionAmount() {
        return transactionRepository.getTotalTransactionAmount();
    }

    // ===== OPERAȚIUNI ADMINISTRATIVE =====

    /**
     * Marchează o tranzacție ca finalizată
     */

    public Transaction markAsCompleted(String transactionId) {
        Transaction transaction = findTransaction(transactionId);

        if (transaction.isPending()) {
            transaction.markAsCompleted();
            return transactionRepository.save(transaction);
        }

        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzacția nu poate fi marcată ca finalizată. Status curent: " + transaction.getStatus());
    }

    /**
     * Marchează o tranzacție ca eșuată
     */

    public Transaction markAsFailed(String transactionId, String reason) {
        Transaction transaction = findTransaction(transactionId);

        if (transaction.isPending()) {
            transaction.markAsFailed();

            String newDescription = transaction.getDescription() + " (Eșuat: " + reason + ")";

            return transactionRepository.save(transaction);
        }

        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzacția nu poate fi marcată ca eșuată. Status curent: " + transaction.getStatus());
    }

    /**
     * Anulează o tranzacție
     */

    public Transaction cancelTransaction(String transactionId, String reason) {
        Transaction transaction = findTransaction(transactionId);

        if (transaction.isPending()) {
            transaction.markAsCancelled();

            if (transaction.getType() == Transaction.TransactionType.TRANSFER_OUT ||
                    transaction.getType() == Transaction.TransactionType.TRANSFER_IN) {
            }

            return transactionRepository.save(transaction);
        }

        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzacția nu poate fi anulată. Status curent: " + transaction.getStatus());
    }

    //@Cacheable(value = "transactionStats", key = "'countByType'")
    public Map<Transaction.TransactionType, Long> getTransactionCountByType() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<Transaction.TransactionType, Long> countByType = new HashMap<>();

        for (Transaction.TransactionType type : Transaction.TransactionType.values()) {
            countByType.put(type, 0L);
        }

        for (Transaction transaction : allTransactions) {
            countByType.merge(transaction.getType(), 1L, Long::sum);
        }

        return countByType;
    }

    public Map<Currency, BigDecimal> getTotalAmountByCurrency() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<Currency, BigDecimal> totalByCurrency = new HashMap<>();

        for (Currency currency : Currency.values()) {
            totalByCurrency.put(currency, BigDecimal.ZERO);
        }

        for (Transaction transaction : allTransactions) {
            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                totalByCurrency.merge(
                        transaction.getCurrency(),
                        transaction.getAmount(),
                        BigDecimal::add
                );
            }
        }

        return totalByCurrency;
    }

    public List<Account> getTopActiveAccounts(int limit) {

        Map<String, Long> transactionCountByAccount = new HashMap<>();

        for (Transaction transaction : transactionRepository.findAll()) {
            if (transaction.getSourceAccountNumber() != null) {
                transactionCountByAccount.merge(
                        transaction.getSourceAccountNumber(), 1L, Long::sum);
            }
            if (transaction.getTargetAccountNumber() != null) {
                transactionCountByAccount.merge(
                        transaction.getTargetAccountNumber(), 1L, Long::sum);
            }
        }

        return transactionCountByAccount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    try {
                        return accountService.findAccount(entry.getKey());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<LocalDate, Long> getDailyTransactionCount(int days) {
        Map<LocalDate, Long> dailyCount = new LinkedHashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Transaction> transactions = transactionRepository
                .findByTimestampBetween(startDate, endDate);

        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getTimestamp().toLocalDate();
            dailyCount.merge(date, 1L, Long::sum);
        }

        return dailyCount;
    }

    public Map<LocalDate, BigDecimal> getDailyTransactionVolume(int days) {
        Map<LocalDate, BigDecimal> dailyVolume = new LinkedHashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Transaction> transactions = transactionRepository
                .findByTimestampBetween(startDate, endDate);

        for (Transaction transaction : transactions) {
            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                LocalDate date = transaction.getTimestamp().toLocalDate();
                BigDecimal amountInMDL = transaction.getAmountInMDL();
                dailyVolume.merge(date, amountInMDL, BigDecimal::add);
            }
        }

        return dailyVolume;
    }

    public BigDecimal getAverageTransactionAmount() {
        BigDecimal total = getTotalTransactionAmount();
        long count = transactionRepository.count();
        return count > 0 ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public List<Transaction> getLargestTransactions(int limit) {
        return transactionRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getAmountInMDL().compareTo(t1.getAmountInMDL()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<Integer, Long> getTransactionCountByHour() {
        Map<Integer, Long> countByHour = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            countByHour.put(i, 0L);
        }

        for (Transaction transaction : transactionRepository.findAll()) {
            int hour = transaction.getTimestamp().getHour();
            countByHour.merge(hour, 1L, Long::sum);
        }

        return countByHour;
    }

    public Map<String, Object> getCompleteTransactionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTransactions", transactionRepository.count());
        stats.put("totalAmount", getTotalTransactionAmount());
        stats.put("averageAmount", getAverageTransactionAmount());
        stats.put("countByType", getTransactionCountByType());
        stats.put("amountByCurrency", getTotalAmountByCurrency());
        stats.put("pendingTransactions",
                transactionRepository.findPendingTransactions().size());
        stats.put("failedTransactions",
                transactionRepository.findFailedTransactions().size());
        stats.put("topAccounts", getTopActiveAccounts(5));
        stats.put("largestTransactions", getLargestTransactions(5));

        return stats;
    }
}