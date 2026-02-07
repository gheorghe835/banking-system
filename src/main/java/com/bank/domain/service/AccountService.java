package com.bank.domain.service;

import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.BankingErrorCode;
import com.bank.domain.exception.BankingException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Serviciu pentru gestionarea conturilor bancare
 * Contine ligica de buisiness pentru operatiunile cu conturi
 */

public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ValidationService validationService;

    //constructor cu depedency injection
    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          ValidationService validationService){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.validationService = validationService;
    }

    //operatiuni de baza pe conturi
    /**
     * creeaza un nou cont bancar
     */
    public Account createAccount(String accountNumber, Customer owner,
                                 String accountType, BigDecimal initialBalance){
        //validare input
        validationService.validateAccountNumber(accountNumber);
        validationService.validateCustomer(owner);

        //verifica daca contul exista deja
        if (accountRepository.existsByAccountNumber(accountNumber)){
            throw new BankingException(BankingErrorCode.ACCOUNT_ALREADY_EXISTS,
                    "Contul cu numarul " + accountNumber + " exista deja");
        }

        //creaza contul
        Account account = new Account(accountNumber,owner,accountType,initialBalance);

        //salveaza contul
        Account savedAccount = accountRepository.save(account);

        //inregistreaza tranzactia de creare
        Transaction transaction = new Transaction(Transaction.TransactionType.ACCOUNT_CREATION,
                initialBalance, Currency.MDL,"Creare cont cu sold initial");
        transaction.setSourceAccountNumber(null);
        transaction.setTargetAccountNumber(accountNumber);
        transaction.markASCompleted();
        transactionRepository.save(transaction);

        return savedAccount;
    }

    /**
     * Gaseste un cont dupa numar
     */
    public Account findAccount(String accountNumber){
        validationService.validateAccountNumber(accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    /**
     * Gaseste un cont dupa numar cu verificare de activitate
     */
    public Account findActiveAccount(String accountNumber){
        Account account = findAccount(accountNumber);

        if (!account.isActive()){
            throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                    "Contul "+ accountNumber + " este inactiv");
        }

        return account;
    }

    /**
     * Returneaza toate conturile
     */
    public List<Account> getAllAccounts(){return accountRepository.findAll();}

    /**
     * returneaza conturile unui client
    */
    public List<Account> getCustomerAccounts(String customerId){
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * sterge un cont(doar daca soldul este zero
     */
    public boolean deleteAccount(String accountNumber){
        Account account = findAccount(accountNumber);

        //verifica daca contul are sold
        if (account.getBalance(Currency.MDL).compareTo(BigDecimal.ZERO) > 0){
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Contul nu poate fi sters deoarece are sold. Transferati mai intii soldul");
        }

        //marcheaza contul ca inactiv in loc de stergere fizica
        account.deactivate();
        accountRepository.save(account);

        return true;
    }

    //operatiuni financiare
    /**
     * depune bani intr-un cont
     */
    public Account deposit(String accountNumber,BigDecimal amount,Currency currency){
        validationService.validateDepositAmount(amount.doubleValue(),currency);

        Account account = findActiveAccount(accountNumber);

        //efectuiaza depunerea
        if (account.deposit(amount,currency)){
            account = accountRepository.save(account);

            //inregistreaza tranzactia
            Transaction transaction = new Transaction(Transaction.TransactionType.DEPOSIT,
                    amount,
                    currency,
                    "Depunere in cont");
            transaction.setSourceAccountNumber(null);
            transaction.setTargetAccountNumber(accountNumber);
            transaction.markASCompleted();
            transactionRepository.save(transaction);

            return account;
        }
        throw new BankingException(BankingErrorCode.TRANSACTION_FAILED,
                "Depunerea a esuat pentru contul " + accountNumber);
    }

    /**
     * retrage banii dintr-un cont
     */
    public Account withdraw(String accountNumber,BigDecimal amount,Currency currency){
        validationService.validateWithdrawalAmount(amount.doubleValue(),currency);

        Account account = findActiveAccount(accountNumber);

        //efectuiaza retragerea
        if (account.withdraw(amount,currency)){
            account = accountRepository.save(account);

            //inregistreaza tranzactia
            Transaction transaction = new Transaction(Transaction.TransactionType.WITHDRAWAL,
                    amount,
                    currency,
                    "Retragere din cont");
            transaction.setSourceAccountNumber(accountNumber);
            transaction.setTargetAccountNumber(null);
            transaction.markASCompleted();
            transactionRepository.save(transaction);

            return account;
        }
        throw new BankingException(BankingErrorCode.TRANSACTION_FAILED,
                "Retragerea a esuat pentru contul "+ accountNumber);
    }

    /**
     * transfera bani intre doua conturi
     */
    public void transfer(String sourceAccountNumber,String targetAccountNumber,
                         BigDecimal amount,Currency currency,String description){
        validationService.validateAccountNumber(sourceAccountNumber);
        validationService.validateAccountNumber(targetAccountNumber);
        validationService.validateWithdrawalAmount(amount.doubleValue(),currency);

        if (sourceAccountNumber.equals(targetAccountNumber)){
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Nu puteti transfera bani catre acelasi cont");
        }

        Account sourceAccount = findActiveAccount(sourceAccountNumber);
        Account targetAccount = findActiveAccount(targetAccountNumber);

        //efectuiaza transferul
        if (sourceAccount.transferTo(targetAccount,amount,currency,description)){
            //salveaza ambele conturi
            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);

            //transferul a fost deja inregistrat in metoda transferTo() a contului
        }
        else {
            throw new BankingException(BankingErrorCode.TRANSACTION_FAILED,
                    "Transferul a esuat");
        }
    }

    /**
     * verifica soldul unui cont
     */
    public BigDecimal getBalance(String accountNumber,Currency currency){
        Account account = findActiveAccount(accountNumber);
        return account.getBalance(currency);
    }

    /**
     * verifica soldul total in MDL al unui cont
     */
    public BigDecimal getTotalBalanceInMDL(String accountNumber){
        Account account = findActiveAccount(accountNumber);
        return account.getTotalBalancesInMDL();
    }

    //Operatiuni administrative
    /**
     * blocheaza un cont
     */
    public Account blockAccount(String accountNumber){
        Account account = findAccount(accountNumber);

        if (!account.isActive()){
            throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                    "Contul este deja inactiv");
        }

        account.deactivate();
        Account blockedAccount = accountRepository.save(account);

        //inregistreaza evenimentul
        Transaction transaction = new Transaction(Transaction.TransactionType.ACCOUNT_DEACTIVATED,
                BigDecimal.ZERO,
                Currency.MDL,
                "Cont blocat");
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markASCompleted();
        transactionRepository.save(transaction);

        return blockedAccount;
    }

    //deblocheaza un cont
    public Account unblockAccount(String accountNumber){
        Account account = findAccount(accountNumber);

        if (!account.isActive()){
            throw new BankingException(BankingErrorCode.INVALID_TRANSACTION,
                    "Contul este deja activ");
        }
        account.activate();
        Account unblockedAccount = accountRepository.save(account);

        //inregistreaza evenimentul
        Transaction transaction = new Transaction(Transaction.TransactionType.ACCOUNT_REACTIVATED,
                BigDecimal.ZERO,
                Currency.MDL,
                "Cont deblocat");
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markASCompleted();
        transactionRepository.save(transaction);

        return unblockedAccount;
    }

    /**
     * actualizeaza limita zilnica de retragere
     */
    public Account updateDailyWithdrawalLimit(String accountNumber,BigDecimal newLimit){
        validationService.validateWithdrawalLimit(newLimit.doubleValue());

        Account account = findActiveAccount(accountNumber);
        account.setDailyWithdrawalLimit(newLimit);

        return accountRepository.save(account);
    }

    /**
     * schimba numele proprietarului contului
     */
    public Account updateAccountOwner(String accountNumber, Customer newOwner) {
        // Validare
        if (newOwner == null) {
            throw new ValidationException("Proprietar invalid")
                    .addError("owner", "Proprietarul nu poate fi null", null);
        }

        // Validare nume
        if (newOwner.getFirstName() == null || newOwner.getFirstName().trim().length() < 2 ||
                newOwner.getLastName() == null || newOwner.getLastName().trim().length() < 2) {
            throw new ValidationException("Nume proprietar invalid")
                    .addError("firstName", "Prenumele trebuie să aibă minim 2 caractere", newOwner.getFirstName())
                    .addError("lastName", "Numele trebuie să aibă minim 2 caractere", newOwner.getLastName());
        }

        Account account = findActiveAccount(accountNumber);

        // Actualizează proprietarul
        account.setOwner(newOwner);

        return accountRepository.save(account);
    }

    //Rapoarte si statistici
    /**
     * genereaza raport cu toate conturile active
     */
    public List<Account> getActiveAccounts(){
        return accountRepository.findActiveAccounts();
    }

    /**
     * genereaza raport cu toate conturile inactive
     */
    public List<Account> getInactiveAccounts(){
        return accountRepository.findInactiveAccounts();
    }

    /**
     * renturneaza conturile cu sold peste a anumita valoare
     */
    public List<Account> getAccountsWithBalanceAbove(double minBalance){
        return accountRepository.findByBalanceGreaterThanEqual(minBalance);
    }

    /**
     * returneaza soldul total in MDL al tuturor conturilor
     */
    public BigDecimal getTotalBankBalance(){
        return BigDecimal.valueOf(accountRepository.getTotalBalanceInMDL());
    }

    /**
     * returneaza numarul total de conturi
     */
    public long getTotalAccountCount(){
        return accountRepository.count();
    }

    /**
     * returneaza numarul de conturi active
     */
    public long getActiveAccountCount(){
        return accountRepository.findActiveAccounts().size();
    }

    /**
     * actualizeaza data ultimei autentificari
     */
    public void updateLastLogin(String accountNumber){
        Account account = findActiveAccount(accountNumber);
        account.updateLastLogin();
        accountRepository.save(account);
    }

    /**
     * verifica daca un cont are suficiente fonduri
     */
    public boolean hasSufficientFunds(String accountNumber, BigDecimal amount,Currency currency){
        Account account = findActiveAccount(accountNumber);
        return account.hasSufficientFounds(amount,currency);
    }
}
