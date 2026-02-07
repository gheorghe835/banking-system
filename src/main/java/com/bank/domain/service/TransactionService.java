package com.bank.domain.service;

import com.bank.domain.exception.BankingErrorCode;
import com.bank.domain.exception.BankingException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviciu pentru gestionarea tranzactiilor bancare
 */

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService){
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    //Operatiuni de baza pe tranzactii
    /**
     * gaseste o tranzactie dupa ID
     */
    public Transaction findTransaction(String transactionId){
        return transactionRepository.findById(transactionId)
                .orElseThrow(()->new BankingException(BankingErrorCode.NOT_FOUND,
                        "Tranzactia cu ID " +transactionId + " nu a fost gasita"));
    }

    /**
     * returneaza toate tranzactiile
     */
    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }

    /**
     * returneaza tranzactiile unui cont
     */
    public List<Transaction> getAccountTransactions(String accountNumber){
        return transactionRepository.findByAccountNumber(accountNumber);
    }

    /**
     * returneaza ultimele N tranzactii ale unui cont
     */
    public List<Transaction> getLastTransactions(String accountNumber,int limit){
        if (limit <= 0 ||limit > 100){
            throw new ValidationException("Limita invalida")
                    .addError("limit","Limita trebuie sa fie intre 1 si 100",limit);
        }
        return transactionRepository.findLastTransactionByAccount(accountNumber,limit);
    }

    //Rapoarte si istoric
    /**
     * genereaza extras de cont pentru o perioada
     */
    public List<Transaction> generateAccountStatement(String accountNumber,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate){
        //validare date
        if (startDate.isAfter(endDate)){
            throw new ValidationException("Perioada invalida")
                    .addError("startDate","Data de inceput trebuie sa fie inainte de data de sfirsit",startDate)
                    .addError("endDate","Data de sfirsit trebuie sa fie dupa data de inceput",endDate);
        }
        //verifica daca contul exista
        accountService.findAccount(accountNumber);

        return transactionRepository.generateAccountStatement(accountNumber,startDate,endDate);
    }

    /**
     * returneaza tranzactiile dintr-o perioada
     */
    public List<Transaction> getTransactionsBetween(LocalDateTime startDate,LocalDateTime endDate){
        return transactionRepository.findByTimestampBetween(startDate,endDate);
    }

    /**
     * returneaza tranzactiile de un anumit tip
     */
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type){
        List<Transaction> allTransactions = transactionRepository.findAll();
        return allTransactions.stream()
                .filter(t->t.getType() == type)
                .toList();
    }

    /**
     * returneaza suma totala depusa intr-un cont
     */
    public double getTotalDeposits(String accounNumber){
        return transactionRepository.getTotalDepositsForAccount(accounNumber);
    }

    /**
     * returneaza suma totala retrasa dintr-un cont
     */
    public double getTotalWithdrawals(String accountNumber){
        return transactionRepository.getTotalaWithdrawalsForAccount(accountNumber);
    }

    /**
     * calculeaza fluxul de numerar pentru un cont
     */
    public double getNetCashFlow(String accountNumber){
        double deposits = getTotalDeposits(accountNumber);
        double withdrawals = getTotalWithdrawals(accountNumber);
        return deposits - withdrawals;
    }

    //Statistici
    /**
     * returneaza numarul total de tranzactii
     */
    public long getTotalTransactionsCount(){
        return transactionRepository.count();
    }

    /**
     * returneaza numarul de tranzactii pentru un cont
     */
    public long getTransactionCountForAccount(String accountNumber){
        return transactionRepository.countByAccountNumber(accountNumber);
    }

    /**
     * returneaza tranzactiile esuate
     */
    public List<Transaction> getFailedTransactions(){
        return transactionRepository.findFailedTransaction();
    }

    /**
     * returneaza tranzactiile in asteptare
     */
    public List<Transaction> getPendingTransactions(){
        return transactionRepository.findPendingTransaction();
    }

    /**
     * returneaza suma totala a tuturor tranzactiilor
     */
    public double getTotalTransactionAmount(){
        return transactionRepository.getTotalTransactionAmount();
    }

    //Operatiuni administrative
    /**
     * marcheaza o operatiune ca finalizata
     */
    public Transaction markAsCompleted(String transactionId){
        Transaction transaction = findTransaction(transactionId);

        if (transaction.isPending()){
            transaction.markASCompleted();
            return transactionRepository.save(transaction);
        }

        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzactia nu poate fi marcata ca finalizata. Status curent: " + transaction.getStatus());
    }

    /**
     * marcheaza o tranzactie ca esuata
     */
    public Transaction markAsFailed(String transactionId,String reason){
        Transaction transaction = findTransaction(transactionId);

        if (transaction.isPending()){
            transaction.markAsFailed();

            //actualizeaza descrierea cu motivul esuarii
            String newDescription = transaction.getDescription() + " (Esuat: " + reason + ")";
            return transactionRepository.save(transaction);
        }
        throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                "Tranzactia nu poate fi marcata ca esuata. Status curent: " + transaction.getStatus());
    }

    /**
     * anualeaza o tranzactie
     */
    public Transaction cancelTransaction(String transactionId, String reason) {
        // 1. Găsește tranzacția
        Transaction transaction = findTransaction(transactionId);

        // 2. Verifică dacă poate fi anulată (doar dacă e în așteptare)
        if (!transaction.isPending()) {
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Doar tranzacțiile în așteptare pot fi anulate. Status curent: " +
                            transaction.getStatus());
        }

        // 3. Marchează ca anulată
        transaction.markAsCancelled();

        // 4. Adaugă motivul anulării în descriere (simplu)
        String currentDesc = transaction.getDescription();
        String newDesc = currentDesc + " [ANULAT: " + reason + "]";

        // Notă: Dacă Transaction nu are setDescription, poți să salvezi altfel

        // 5. Pentru transferuri, doar afișează un mesaj (fără rollback automat)
        if (transaction.getType() == Transaction.TransactionType.TRANSFER_OUT ||
                transaction.getType() == Transaction.TransactionType.TRANSFER_IN) {

            System.out.println("⚠️ ATENȚIE: Transfer anulat!");
            System.out.println("   ID: " + transactionId);
            System.out.println("   Suma: " + transaction.getAmount() + " " +
                    transaction.getCurrency());
            System.out.println("   Motiv: " + reason);
            System.out.println("   Contactați un manager pentru returnarea banilor.");
        }

        // 6. Salvează tranzacția actualizată
        return transactionRepository.save(transaction);
    }
}
