package com.bank.domain.repository;

import com.bank.domain.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * interfata pentru repository de tranzactii bancare
 */

public interface TransactionRepository {
//operatiuni de baza (CRUD)

    /**
     * salveaza o tranzactie
     * @param transaction tranzactia de salvat
     * @return tranzactia salvata
     */
    Transaction save(Transaction transaction);

    /**
     * gaseste o tranzactie dupa ID
     * @param transactionId ID tranzactiei
     * @return Optional care contine tranzactia daca este gasita
     */
    Optional<Transaction> findById(String transactionId);

    /**
     * sterge o tranzactie dupa ID
     * @param transactionId ID tranzactiei de sters
     * @return true daca tranzactia a fost stearsa
     */
    boolean deleteById(String transactionId);

    /**
     * returneaza toate tranzactiile
     * @return lista tuturor tranzactiilor
     */
    List<Transaction> findAll();
    boolean existsById(String transactionId);

    /**
     * returneaza numarul total de tranzactii
     * @return numarul de tranzactii
     */
    long count();

    //metode specifice buisiness bancar

    /**
     * gaseste toate tranzactiile unui cont
     * @param accountNumber numarul contului
     * @return lista tranzactiilor contului
     */
    List<Transaction> findByAccountNumber(String accountNumber);

    /**
     * gaseste tranzactiile unui cont de un anumit tip
     * @param accountNumber numarul contului
     * @param transactionType tipul tranzactiei
     * @return lista tranzactiilor filtrate
     */
    List<Transaction> findByAccountNumberAndType(String accountNumber,
                                                 Transaction.TransactionType transactionType);

    /**
     * gaseste tranzactiile dintr-o perioada de timp
     * @param startDate data de inceput
     * @param endDate data de sfirsit
     * @return lista tranzactiilor din perioada specificata
     */
    List<Transaction> findByTimestampBetween(LocalDateTime startDate,LocalDateTime endDate);

    /**
     * gaseste tranzactiile cu suma mai decit o valoare
     * @param minAmount suma minima
     * @return lista tranzactiilor cu suma >= minAmount
     */
    List<Transaction> findByAmountGreaterThanEqual(double minAmount);

    /**
     * gaseste ulimele N tranzactii ale unui cont
     * @param accountNumber numarul contului
     * @param limit numarul maxim de tranzactii
     * @return numarul maxim de tranzactii
     */
    List<Transaction> findLastTransactionByAccount(String accountNumber,int limit);

    /**
     * gaseste toate transferurile intre doua conturi
     * @param sourceAccount contul sursa
     * @param targetAccount contul destinatie
     * @return lista transferurilor intre conturi
     */
    List<Transaction> findTransfersBetweenAccounts(String sourceAccount,String targetAccount);

    /**
     * gaseste toate tranzactiile esuate
     * @return lista tranzactiilor esuate
     */
    List<Transaction> findFailedTransaction();

    /**
     * gaseste toate tranzactiile in asteptare
     * @return lista tranzactiilor in asteptare
     */
    List<Transaction> findPendingTransaction();

    /**
     * gaseste toate tranzactiile finalizate cu succes
     * @return lista trazactiilor finalizate
     */
    List<Transaction> findCompletedTransaction();

    /**
     * returneaza suma totala a depunirilor pentru un cont
     * @param accountNumber  numarul contului
     * @return suma totala depusa
     */
    double getTotalDepositsForAccount(String accountNumber);

    /**
     * returneaza suma totala a retragerilor pentru un cont
     * @param accountNumber numarul contului
     * @return suma totala retrasa
     */
    double getTotalWithdrawalsForAccount(String accountNumber);

    /**
     * returneaza numarul de tranzactii pentru un cont
     * @param accountNumber numarul contului
     * @return numarul de tranzactii
     */
    long countByAccountNumber(String accountNumber);

    /**
     * genereaza un extras de cont pentru o perioada
     * @param accountNumber numarul contului
     * @param startDate data de inceput
     * @param endDate data de sfirsit
     * @return lista tranzactiilor din perioada
     */
    List<Transaction> generateAccountStatement(String accountNumber,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);

    /**
     * returneaza suma totala a tuturor tranzactiilor
     * @return suma totala
     */
    double getTotalTransactionAmount();

    /**
     * returneaza tranzactiile pentru a anumita moneda
     * @param currency codul monedei(MDL, EUR, USD, etc)
     * @return lista tranzactiilor in moneda specificata
     */
    List<Transaction> findByCurrency(String currency);

    /**
     * marcheaza o tranzactie ca finalizata
     * @param transactionId ID tranzactiei
     * @return true daca a fost marcata cu succes
     */
    boolean markAsCompleted(String transactionId);

    /**
     * marcheaza o tranzactie ca esuata
     * @param transactionId ID tranzactiei
     * @return true daca a fost marcata cu succes
     */
    boolean markAsFailed(String transactionId);

    /**
     * gaseste tranzactiile cu o anumita descriere
     * @param descriptionPart parte din descriere
     * @return lista tranzactiilor care detin descrierea
     */

    List<Transaction> findByDescriptionContaining(String descriptionPart);

    boolean markAsCancelled(String transactionId);
    /**
     * Găsește tranzacțiile după tip
     * @param type Tipul tranzacției (enum value)
     * @return Lista tranzacțiilor filtrate
     */
    List<Transaction> findByType(Transaction.TransactionType type);
}
