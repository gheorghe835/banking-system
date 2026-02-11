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
     * @param account Contul de salvat
     * @return Contul salvat
     */
    Account save(Account account);

    /**
     * Găsește un cont după numărul său
     * @param accountNumber Numărul contului
     * @return Optional care conține contul dacă este găsit
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Șterge un cont după numărul său
     * @param accountNumber Numărul contului de șters
     * @return true dacă contul a fost șters
     */
    boolean deleteByAccountNumber(String accountNumber);

    /**
     * Verifică dacă există un cont cu numărul dat
     * @param accountNumber Numărul contului
     * @return true dacă există
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Returnează toate conturile
     * @return Lista tuturor conturilor
     */
    List<Account> findAll();

    /**
     * Returnează numărul total de conturi
     * @return Numărul de conturi
     */
    long count();

    // ===== BUSINESS SPECIFIC METHODS =====

    /**
     * Găsește toate conturile unui client
     * @param customer Clientul
     * @return Lista conturilor clientului
     */
    List<Account> findByCustomer(Customer customer);

    /**
     * Găsește conturile după tip
     * @param accountType Tipul contului (CURRENT, SAVINGS, BUSINESS)
     * @return Lista conturilor de tipul specificat
     */
    List<Account> findByAccountType(String accountType);

    /**
     * Găsește conturile active
     * @return Lista conturilor active
     */
    List<Account> findActiveAccounts();

    /**
     * Găsește conturile inactive
     * @return Lista conturilor inactive
     */
    List<Account> findInactiveAccounts();

    /**
     * Găsește conturile cu sold peste o anumită valoare în MDL
     * @param minBalance Soldul minim în MDL
     * @return Lista conturilor cu sold >= minBalance
     */
    List<Account> findByBalanceGreaterThanEqual(BigDecimal minBalance);

    /**
     * Găsește conturile create într-o anumită perioadă
     * @param startDate Data de început
     * @param endDate Data de sfârșit
     * @return Lista conturilor create în perioada specificată
     */
    List<Account> findByCreationDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Găsește conturile după proprietar (nume parțial)
     * @param ownerNamePart Parte din numele proprietarului
     * @return Lista conturilor ale căror proprietari conțin șirul dat
     */
    List<Account> findByOwnerNameContaining(String ownerNamePart);

    /**
     * Returnează soldul total MDL din toate conturile
     * @return Soldul total în MDL
     */
    BigDecimal getTotalBalanceInMDL();

    /**
     * Returnează soldul mediu MDL pe cont
     * @return Soldul mediu în MDL
     */
    BigDecimal getAverageBalanceInMDL();

    /**
     * Blochează un cont (îl face inactiv)
     * @param accountNumber Numărul contului de blocat
     * @return true dacă contul a fost blocat
     */
    boolean blockAccount(String accountNumber);

    /**
     * Deblochează un cont (îl face activ)
     * @param accountNumber Numărul contului de deblocat
     * @return true dacă contul a fost deblocat
     */
    boolean unblockAccount(String accountNumber);

    /**
     * Actualizează limita zilnică de retragere pentru un cont
     * @param accountNumber Numărul contului
     * @param newLimit Noua limită în MDL
     * @return true dacă limita a fost actualizată
     */
    boolean updateDailyWithdrawalLimit(String accountNumber, double newLimit);

    /**
     * Resetează limita zilnică utilizată pentru toate conturile
     * (trebuie apelată zilnic)
     * @return Numărul de conturi actualizate
     */
    int resetDailyWithdrawalUsed();
    List<Account> findByCustomerId(String customerId);
}