package com.bank.domain.repository;

import com.bank.domain.model.Account;
import com.bank.domain.model.Customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfață pentru repository-ul de conturi bancare
 * Definește operațiunile CRUD și metode specifice business-ului bancar
 */
public interface AccountRepository {

    // ===== CRUD OPERATIONS =====

    /**
     * Salvează sau actualizează un cont bancar
     */
    Account save(Account account);

    /**
     * Găsește un cont după numărul său
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Șterge un cont după numărul său
     */
    boolean deleteByAccountNumber(String accountNumber);

    /**
     * Verifică dacă există un cont cu numărul dat
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Returnează toate conturile
     */
    List<Account> findAll();

    /**
     * Returnează numărul total de conturi
     */
    long count();

    // ===== BUSINESS SPECIFIC METHODS =====

    /**
     * Găsește toate conturile unui client
     */
    List<Account> findByCustomer(Customer customer);

    /**
     * Găsește conturile după tip
     */
    List<Account> findByAccountType(String accountType);

    /**
     * Găsește conturile active
     */
    List<Account> findActiveAccounts();

    /**
     * Găsește conturile inactive
     */
    List<Account> findInactiveAccounts();

    /**
     * Găsește conturile cu sold peste o anumită valoare în MDL
     */
    List<Account> findByBalanceGreaterThanEqual(BigDecimal minBalance);

    /**
     * Găsește conturile create într-o anumită perioadă
     */
    List<Account> findByCreationDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Găsește conturile după proprietar (nume parțial)
     */
    List<Account> findByOwnerNameContaining(String ownerNamePart);

    /**
     * Returnează soldul total MDL din toate conturile
     */
    BigDecimal getTotalBalanceInMDL();

    /**
     * Returnează soldul mediu MDL pe cont
     */
    BigDecimal getAverageBalanceInMDL();

    /**
     * Blochează un cont (îl face inactiv)
     */
    boolean blockAccount(String accountNumber);

    /**
     * Deblochează un cont (îl face activ)
     */
    boolean unblockAccount(String accountNumber);

    /**
     * Actualizează limita zilnică de retragere pentru un cont
     */
    boolean updateDailyWithdrawalLimit(String accountNumber, double newLimit);

    /**
     * Resetează limita zilnică utilizată pentru toate conturile
     * (trebuie apelată zilnic)
     */
    int resetDailyWithdrawalUsed();
    List<Account> findByCustomerId(String customerId);


}