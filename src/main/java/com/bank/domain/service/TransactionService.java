package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.*;
import com.bank.domain.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviciu pentru gestionarea tranzacțiilor bancare
 */
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
        // Notă: Această metodă necesită o implementare în repository
        // Pentru moment, filtrăm manual
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

            // Actualizează descrierea cu motivul eșuării
            String newDescription = transaction.getDescription() + " (Eșuat: " + reason + ")";
            // Notă: Ar trebui să avem o metodă setDescription în Transaction

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

            // În cazul unui transfer, ar trebui să returnăm banii
            if (transaction.getType() == Transaction.TransactionType.TRANSFER_OUT ||
                    transaction.getType() == Transaction.TransactionType.TRANSFER_IN) {
                // Logica de returnare a banilor ar fi aici
                // Pentru simplitate, doar marcam ca anulat
            }

            return transactionRepository.save(transaction);
        }

        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzacția nu poate fi anulată. Status curent: " + transaction.getStatus());
    }
}