package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.*;
import com.bank.domain.model.Currency;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviciu pentru gestionarea conturilor bancare
 * Conține toată logica de business pentru operațiunile cu conturi
 */
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ValidationService validationService;

    // Constructor cu dependency injection
    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          ValidationService validationService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.validationService = validationService;
    }

    /////////////
    Transaction transaction = new Transaction(
            Transaction.TransactionType.ACCOUNT_DEACTIVATED,
            "Cont blocat"
    );

    // ===== OPERAȚIUNI DE BAZĂ PE CONTURI =====

    /**
     * Creează un nou cont bancar
     */
    public Account createAccount(String accountNumber, Customer owner,
                                 String accountType, BigDecimal initialBalance) {
        // Validare input
        validationService.validateAccountNumber(accountNumber);
        validationService.validateCustomer(owner);
        validationService.validateDepositAmount(initialBalance, Currency.MDL);

        // Verifică dacă contul există deja
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new BankingException(BankingErrorCode.ACCOUNT_ALREADY_EXISTS,
                    "Contul cu numărul " + accountNumber + " există deja");
        }

        // Creează contul
        Account account = new Account(accountNumber, owner, accountType, initialBalance);

        // Salvează contul
        Account savedAccount = accountRepository.save(account);

        // Înregistrează tranzacția de creare
        Transaction transaction = new Transaction(
                Transaction.TransactionType.ACCOUNT_CREATION,
                initialBalance,
                Currency.MDL,
                "Creare cont cu sold inițial"
        );
        transaction.setSourceAccountNumber(null);
        transaction.setTargetAccountNumber(accountNumber);
        transaction.markAsCompleted();
        transactionRepository.save(transaction);

        return savedAccount;
    }

    /**
     * Găsește un cont după număr
     */
    public Account findAccount(String accountNumber) {
        validationService.validateAccountNumber(accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    /**
     * Găsește un cont după număr cu verificare de activitate
     */
    public Account findActiveAccount(String accountNumber) {
        Account account = findAccount(accountNumber);

        if (!account.isActive()) {
            throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                    "Contul " + accountNumber + " este inactiv");
        }

        return account;
    }

    /**
     * Returnează toate conturile
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Returnează conturile unui client
     */
    @Transactional(readOnly = true)
    public List<Account> getCustomerAccounts(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Șterge un cont (doar dacă soldul este zero)
     */
    public boolean deleteAccount(String accountNumber) {
        Account account = findAccount(accountNumber);

        // Verifică dacă contul are sold
        if (account.getBalance(Currency.MDL).compareTo(BigDecimal.ZERO) > 0) {
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Contul nu poate fi șters deoarece are sold. Transferați mai întâi banii.");
        }

        // Marchează contul ca inactiv în loc de ștergere fizică
        account.deactivate();
        accountRepository.save(account);

        return true;
    }

    // ===== OPERAȚIUNI FINANCIARE =====

    /**
     * Depune bani într-un cont
     */
    public Account deposit(String accountNumber, BigDecimal amount, Currency currency) {
        validationService.validateDepositAmount(amount, currency);

        Account account = findActiveAccount(accountNumber);

        // Efectuează depunerea
        if (account.deposit(amount, currency)) {
            account = accountRepository.save(account);

            // Înregistrează tranzacția
            Transaction transaction = new Transaction(
                    Transaction.TransactionType.DEPOSIT,
                    amount,
                    currency,
                    "Depunere în cont"
            );
            transaction.setSourceAccountNumber(null);
            transaction.setTargetAccountNumber(accountNumber);
            transaction.markAsCompleted();
            transactionRepository.save(transaction);

            return account;
        }

        throw new BankingException(BankingErrorCode.TRANSACTION_FAILED,
                "Depunerea a eșuat pentru contul " + accountNumber);
    }

    /**
     * Retrage bani dintr-un cont
     */
    public Account withdraw(String accountNumber, BigDecimal amount, Currency currency) {
        validationService.validateWithdrawalAmount(amount, currency);

        Account account = findActiveAccount(accountNumber);

        // Efectuează retragerea
        if (account.withdraw(amount, currency)) {
            account = accountRepository.save(account);

            // Înregistrează tranzacția
            Transaction transaction = new Transaction(
                    Transaction.TransactionType.WITHDRAWAL,
                    amount,
                    currency,
                    "Retragere din cont"
            );
            transaction.setSourceAccountNumber(accountNumber);
            transaction.setTargetAccountNumber(null);
            transaction.markAsCompleted();
            transactionRepository.save(transaction);

            return account;
        }

        throw new BankingException(BankingErrorCode.TRANSACTION_FAILED,
                "Retragerea a eșuat pentru contul " + accountNumber);
    }

    /**
     * Transferă bani între două conturi
     */
    public void transfer(String sourceAccountNumber, String targetAccountNumber,
                         BigDecimal amount, Currency currency, String description) {
        validationService.validateAccountNumber(sourceAccountNumber);
        validationService.validateAccountNumber(targetAccountNumber);
        validationService.validateWithdrawalAmount(amount, currency);

        if (sourceAccountNumber.equals(targetAccountNumber)) {
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Nu puteți transfera bani către același cont");
        }

        Account sourceAccount = findActiveAccount(sourceAccountNumber);
        Account targetAccount = findActiveAccount(targetAccountNumber);

        // Verifică fonduri suficiente
        if (!sourceAccount.hasSufficientFunds(amount, currency)) {
            throw new InsufficientFundsException(sourceAccountNumber,
                    amount, sourceAccount.getBalance(currency), currency);
        }

        // Efectuează transferul
        sourceAccount.withdraw(amount, currency);
        targetAccount.deposit(amount, currency);

        // Salvează conturile
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Înregistrează tranzacțiile
        Transaction outTransaction = new Transaction(
                Transaction.TransactionType.TRANSFER_OUT,
                amount, currency,
                "Transfer către " + targetAccountNumber + ": " + description
        );
        outTransaction.setSourceAccountNumber(sourceAccountNumber);
        outTransaction.setTargetAccountNumber(targetAccountNumber);
        outTransaction.markAsCompleted();
        transactionRepository.save(outTransaction);

        Transaction inTransaction = new Transaction(
                Transaction.TransactionType.TRANSFER_IN,
                amount, currency,
                "Transfer de la " + sourceAccountNumber + ": " + description
        );
        inTransaction.setSourceAccountNumber(sourceAccountNumber);
        inTransaction.setTargetAccountNumber(targetAccountNumber);
        inTransaction.markAsCompleted();
        transactionRepository.save(inTransaction);
    }

    /**
     * Verifică soldul unui cont
     */
    public BigDecimal getBalance(String accountNumber, Currency currency) {
        Account account = findActiveAccount(accountNumber);
        return account.getBalance(currency);
    }

    /**
     * Verifică soldul total în MDL al unui cont
     */
    public BigDecimal getTotalBalanceInMDL(String accountNumber) {
        Account account = findActiveAccount(accountNumber);
        return account.getTotalBalanceInMDL();
    }

    // ===== OPERAȚIUNI ADMINISTRATIVE =====

    /**
     * Blochează un cont
     */
    public Account blockAccount(String accountNumber) {
        Account account = findAccount(accountNumber);

        if (!account.isActive()) {
            throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                    "Contul este deja inactiv");
        }

        account.deactivate();
        Account blockedAccount = accountRepository.save(account);

        /*/ Înregistrează evenimentul
        Transaction transaction = new Transaction(
                Transaction.TransactionType.ACCOUNT_DEACTIVATED,
                BigDecimal.ZERO,
                Currency.MDL,
                "Cont blocat"
        );
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markAsCompleted();
        transactionRepository.save(transaction);*/
        Transaction transaction = new Transaction();
        transaction.setTransactionId("BLK" + System.currentTimeMillis());
        transaction.setType(Transaction.TransactionType.ACCOUNT_DEACTIVATED);
        transaction.setAmount(BigDecimal.ZERO);
        transaction.setCurrency(Currency.MDL);
        transaction.setDescription("Cont blocat");
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markAsCompleted();

        transactionRepository.save(transaction);

        return blockedAccount;
    }

    /**
     * Deblochează un cont
     */
    public Account unblockAccount(String accountNumber) {
        Account account = findAccount(accountNumber);

        if (account.isActive()) {
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Contul este deja activ");
        }

        account.activate();
        Account unblockedAccount = accountRepository.save(account);

        /*/ Înregistrează evenimentul
        Transaction transaction = new Transaction(
                Transaction.TransactionType.ACCOUNT_REACTIVATED,
                BigDecimal.ZERO,
                Currency.MDL,
                "Cont deblocat"
        );
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markAsCompleted();
        transactionRepository.save(transaction);*/
        Transaction transaction = new Transaction();
        transaction.setTransactionId("UNB" + System.currentTimeMillis());
        transaction.setType(Transaction.TransactionType.ACCOUNT_REACTIVATED);
        transaction.setAmount(BigDecimal.ZERO);
        transaction.setCurrency(Currency.MDL);
        transaction.setDescription("Cont deblocat");
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markAsCompleted();

        transactionRepository.save(transaction);

        return unblockedAccount;
    }

    /**
     * Actualizează limita zilnică de retragere
     */
    public Account updateDailyWithdrawalLimit(String accountNumber, BigDecimal newLimit) {
        validationService.validateWithdrawalLimit(newLimit);

        Account account = findActiveAccount(accountNumber);
        account.setDailyWithdrawalLimit(newLimit);

        return accountRepository.save(account);
    }

    /**
     * Schimbă numele proprietarului contului
     */
    public Account updateAccountOwner(String accountNumber, String newOwnerName) {
        if (newOwnerName == null || newOwnerName.trim().length() < 2) {
            throw new ValidationException("Nume proprietar invalid")
                    .addError("ownerName", "Numele trebuie să aibă minim 2 caractere", newOwnerName);
        }

        Account account = findActiveAccount(accountNumber);
        // Notă: În implementarea actuală, Account nu are setOwnerName
        // Ar trebui să adăugăm această metodă sau să lucrăm prin Customer

        return accountRepository.save(account);
    }

    // ===== RAPOARTE ȘI STATISTICI =====

    /**
     * Generează raport cu toate conturile active
     */
    public List<Account> getActiveAccounts() {
        return accountRepository.findActiveAccounts();
    }

    /**
     * Generează raport cu toate conturile inactive
     */
    public List<Account> getInactiveAccounts() {
        return accountRepository.findInactiveAccounts();
    }

    /**
     * Returnează conturile cu sold peste o anumită valoare
     */
    public List<Account> getAccountsWithBalanceAbove(BigDecimal minBalance) {
        return accountRepository.findByBalanceGreaterThanEqual(minBalance);
    }

    /**
     * Returnează soldul total MDL al tuturor conturilor
     */
    public BigDecimal getTotalBankBalance() {
        return accountRepository.getTotalBalanceInMDL();
    }

    /**
     * Returnează numărul total de conturi
     */
    public long getTotalAccountCount() {
        return accountRepository.count();
    }

    /**
     * Returnează numărul de conturi active
     */
    public long getActiveAccountCount() {
        return accountRepository.findActiveAccounts().size();
    }

    /**
     * Actualizează data ultimei autentificări
     */
    public void updateLastLogin(String accountNumber) {
        Account account = findActiveAccount(accountNumber);
        account.updateLastLogin();
        accountRepository.save(account);
    }

    /**
     * Verifică dacă un cont are suficiente fonduri
     */
    public boolean hasSufficientFunds(String accountNumber, BigDecimal amount, Currency currency) {
        Account account = findActiveAccount(accountNumber);
        return account.hasSufficientFunds(amount, currency);
    }

    //////////////////////////////////////////////////////////
    public Map<Currency, BigDecimal> getTotalBalancePerCurrency() {
        List<Account> allAccounts = accountRepository.findAll();
        Map<Currency, BigDecimal> totals = new HashMap<>();

        // Inițializează cu 0
        for (Currency currency : Currency.values()) {
            totals.put(currency, BigDecimal.ZERO);
        }

        // Adună soldurile
        for (Account account : allAccounts) {
            for (Currency currency : Currency.values()) {
                BigDecimal balance = account.getBalance(currency);
                totals.merge(currency, balance, BigDecimal::add);
            }
        }

        return totals;
    }

    public Map<String, Object> getDetailedBankStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAccounts", accountRepository.count());
        stats.put("activeAccounts", accountRepository.findActiveAccounts().size());
        stats.put("inactiveAccounts", accountRepository.findInactiveAccounts().size());
        stats.put("totalBalanceMDL", getTotalBankBalance());
        stats.put("balancePerCurrency", getTotalBalancePerCurrency());
        stats.put("averageBalanceMDL", accountRepository.getAverageBalanceInMDL());
        stats.put("richestAccount", findRichestAccount());
        stats.put("poorestActiveAccount", findPoorestActiveAccount());
        stats.put("accountsCreatedToday", countAccountsCreatedToday());
        stats.put("accountsByType", getAccountsCountByType());

        return stats;
    }

    public Optional<Account> findRichestAccount() {
        return accountRepository.findAll().stream()
                .max(Comparator.comparing(Account::getTotalBalanceInMDL));
    }

    public Optional<Account> findPoorestActiveAccount() {
        return accountRepository.findActiveAccounts().stream()
                .min(Comparator.comparing(Account::getTotalBalanceInMDL));
    }

    public long countAccountsCreatedToday() {
        LocalDate today = LocalDate.now();
        return accountRepository.findByCreationDateBetween(today, today).size();
    }

    public Map<String, Long> getAccountsCountByType() {
        Map<String, Long> countByType = new HashMap<>();

        countByType.put("CURRENT",
                (long) accountRepository.findByAccountType(Account.ACCOUNT_TYPE_CURRENT).size());
        countByType.put("SAVINGS",
                (long) accountRepository.findByAccountType(Account.ACCOUNT_TYPE_SAVINGS).size());
        countByType.put("BUSINESS",
                (long) accountRepository.findByAccountType(Account.ACCOUNT_TYPE_BUSINESS).size());

        return countByType;
    }

    public Account exchangeCurrency(String accountNumber, Currency fromCurrency,
                                    Currency toCurrency, BigDecimal amount) {

        Account account = findActiveAccount(accountNumber);

        // Verifică dacă are suficienți bani în moneda sursă
        if (!account.hasSufficientFunds(amount, fromCurrency)) {
            throw new InsufficientFundsException(accountNumber, amount,
                    account.getBalance(fromCurrency), fromCurrency);
        }

        // Calculează suma în moneda destinație
        BigDecimal amountInMDL = amount.multiply(BigDecimal.valueOf(fromCurrency.getExchangeRateToMDL()));
        BigDecimal targetAmount = amountInMDL.divide(BigDecimal.valueOf(toCurrency.getExchangeRateToMDL()),
                2, RoundingMode.HALF_UP);

        // Efectuează schimbul
        account.withdraw(amount, fromCurrency);
        account.deposit(targetAmount, toCurrency);

        Account updatedAccount = accountRepository.save(account);

        // Înregistrează tranzacția
        Transaction transaction = new Transaction(
                Transaction.TransactionType.CURRENCY_EXCHANGE,
                amount,
                fromCurrency,
                String.format("Schimb %s -> %s", fromCurrency, toCurrency)
        );
        transaction.setSourceAccountNumber(accountNumber);
        transaction.setTargetAccountNumber(accountNumber);
        transaction.markAsCompleted();
        transactionRepository.save(transaction);

        return updatedAccount;
    }

}