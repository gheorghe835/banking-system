package com.bank.domain.repository;

import com.bank.domain.model.Account;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfata pentru repository de conturi bancare
 * Defineste operatiunile CRUD si metode specifice buisiness bancar
 */

public interface AccountRepository {
    //operatiuni de baza - CRUD

    /**
     * salveaza sau actualizeaza un cont bancar
     * @param account Contul de salvat
     * @return Contul salvat
     */
    Account save(Account account);

    /**
     * gaseste un cont dupa numarul sau
     * @param accountNumber numarul contului
     * @return Optional care contine contul daca este gasit
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * sterge un cont dupa numarul sau
     * @param accauntNumber numarul contului de sters
     * @return true daca contul a fost sters
     */
    boolean deleteByAccountNumber(String accauntNumber);

    /**
     * verifica daca exista un cont cu numarul dat
     * @param accauntNumber numarul contului
     * @return true daca exista
     */
    boolean existsByAccountNumber(String accauntNumber);

    /**
     * returneaza toate conturile
     * @return lista tuturor conturilor
     */
    List<Account> findAll();

    /**
     * returneaza numarul total de conturi
     * @return numarul de conturi
     */
    long count();

    //metode specifice buisiness bancar
    /**
     * gaseste toate conturile unui client
     * @param customerId client
     * @return lista conturilor clientului
     */
    List<Account> findByCustomerId(String customerId);

    /**
     * gaseste conturile dupa tip
     * @param accountType tipul contului (CURRENT,SAVINGS,BUISINESS)
     * @return lista conturilor de tipul specificat
     */
    List<Account> findByAccountType(String accountType);

    /**
     * gaseste conturile active
     * @return lista conturilor active
     */
    List<Account> findActiveAccounts();

    /**gaseste conturile inactive
     * @return lista conturilor inactive
     */
    List<Account> findInactiveAccounts();

    /**
     * gaseste conturile cu sold peste o anumita valoare in MDL
     * @param minBalance soldul minim in MDL
     * @return lista conturilor cu sold >= minBalance
     */
    List<Account> findByBalanceGreaterThanEqual(double minBalance);

    /**
     * gaseste conturile create intr-o anumita perioada
     * @param startDate data de inceput
     * @param endDate data de sfirsit
     * @return lista conturilor conturilor create in perioda specificata
     */
    List<Account> findByCreationDateBetween(LocalDate startDate,LocalDate endDate);

    /**
     * gaseste conturile dupa numele proprietarului(cautare partiala)
     * @param ownerNamePart parte din numele proprietarului
     * @return lista conturilor ale caror proprietati contin sirul dat
     */
    List<Account> findByOwnerNameContainig(String ownerNamePart);

    /**
     * returneaza soldul total MDL din toate conturile
     * @return soldul total in MDL
     */
    double getTotalBalanceInMDL();

    /**
     * returneaza soldul mediu MDL pe cont
     * @return soldul mediu in MDL
     */
    double getAverageBalanceInMDL();

    /**
     * gaseste contul cu cel mai mare sold
     * @return contul cu cel mai mare sold
     */
    Optional<Account> findAccountWithMaxBalance();

    /**
     * gaseste contul cu cel mai mic sold
     * @return contul cu cel mai mic sold
     */
    Optional<Account> findAccountWithMinBalance();

    /**
     * blocheaza un cont(inactiv)
     * @param accountNumber numarul contului de blocat
     * @return true daca contul a fost blocat
     */
    boolean blockAccount(String accountNumber);

    /**
     * deblocheaza un cont(activ)
     * @param accountNumber numarul contului de deblocat
     * @return true daca contul a fost deblocat
     */
    boolean unblockAccount(String accountNumber);

    /**
     * actualizeaza limita zilnica de retragere pentru un cont
     * @param accountNumber numarul contului
     * @param newLimit noua limita in MDL
     * @return true daca limita a fost actualizata
     */
    boolean updateDailyWithdrawalLimit(String accountNumber,double newLimit);

    /**
     * reseteaza limita zilnica utilizata pentru toate conturile
     * (trebuie apelata zilnic)
     * @return numarul de conturi actualizate
     */
    int resetDailyWithdrawalUsed();

    /**
     * transfera bani intre doua conturi
     * @param sourceAccountNumber contul sursa
     * @param targetAccountNumber contul destinatie
     * @param amount suma de transferat
     * @param currency moneda transferului
     * @param description descrierea transferului
     * @return true daca transferul a reusit
     */
    boolean transfer(String sourceAccountNumber,String targetAccountNumber,double amount,String currency,String description);
}
