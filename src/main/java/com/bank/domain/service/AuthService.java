package com.bank.domain.service;

import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.BankingErrorCode;
import com.bank.domain.exception.BankingException;
import com.bank.domain.exception.SecurityException;
import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Customer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviciu pentru autentificare si autorizare
 */

public class AuthService {
    private final AccountService accountService;
    private final Map<String,Integer> failedLoginAttempts = new HashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new HashMap<>();

    //constante pentru securitate
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 30;

    //credentiale hardcodate pentru manager
    private static final String MANAGER_USERNAME = "admin";
    private static final String MANAGER_PASSWORD = "Admin1234";

    public AuthService(AccountService accountService){
        this.accountService = accountService;
    }

    //Autentificare client
    /**
     * autentifica un client
     */
    public Account authenticateClient(String accountNumber,String password){
        //verifica daca contul este blocat
        checkIfAccountLocked(accountNumber);

        try {
            //gaseste contul
            Account account = accountService.findAccount(accountNumber);

            //verifica daca contul este activ
            if (!account.isActive()){
                throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                        "Contul este inactiv");
            }

            //verifica parola
            if (!verifyAccouontPassword(account,password)){
                handleFailedLoginAttempt(accountNumber);
                throw new SecurityException(BankingErrorCode.INVALID_CREDENTIALS,
                        accountNumber,SecurityException.SecurityAction.LOGIN_ATTEMPT);
            }

            //reseteaza incercarile esuate la autentificarea reusita
            resetFailedAttempts(accountNumber);

            //actualizeaza ultima data de conectare
            accountService.updateLastLogin(accountNumber);

            return account;
        }
        catch (AccountNotFoundException e){
            //trateaza contul inexistent ca o incercare de conectare esuata
            handleFailedLoginAttempt(accountNumber);
            throw e;
        }
    }

    //verifica parola unui cont(simplificat)
    private boolean verifyAccouontPassword(Account account,String password){
        String expectedPassword = "Parola1234"; //parola default

        return expectedPassword.equals(password);
    }

    //Autentificare manager
    /**
     * autentifica un manager
     */
    public BankManager authenticateManager(String username,String password){
        if (MANAGER_USERNAME.equals(username) && MANAGER_PASSWORD.equals(password)){
            return createManager();
        }
        throw new SecurityException(BankingErrorCode.INVALID_CREDENTIALS,
                username,SecurityException.SecurityAction.LOGIN_ATTEMPT);
    }

    //creaza un manager
    private BankManager createManager(){
        BankManager manager = new BankManager(MANAGER_USERNAME,
                "Admin",
                "System",
        "admin@bank.com",
        BankManager.AccessLevel.ADMIN);
        return manager;
    }

    //Gestiune blochare conturi
    /**
     * verifica dava un cont este blocat
     */
    private void checkIfAccountLocked(String accountNumber){
        if (lockedAccounts.containsKey(accountNumber)){
            LocalDateTime lockTime = lockedAccounts.get(accountNumber);
            LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);

            if (LocalDateTime.now().isBefore(unlockTime)){
                long minutesRemaining = java.time.Duration.between(LocalDateTime.now(),unlockTime).toMinutes();
                throw new SecurityException(BankingErrorCode.ACCOUNT_BLOCKED,
                        accountNumber, SecurityException.SecurityAction.LOGIN_ATTEMPT,
                        "Cont blocat. Incercati din nou peste " + minutesRemaining + " minute");
            }
            else {
                //perioada de blocare a expirat
                lockedAccounts.remove(accountNumber);
                failedLoginAttempts.remove(accountNumber);
            }
        }
    }

    /**
     * gestioneaza o incercare de login esuata
     */
    private void handleFailedLoginAttempt(String accountNumber){
        int attempts = failedLoginAttempts.getOrDefault(accountNumber,0) + 1;
        failedLoginAttempts.put(accountNumber,attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS){
            //blocheaza contul
            lockedAccounts.put(accountNumber,LocalDateTime.now());
            throw new SecurityException(BankingErrorCode.TOO_MANY_ATTEMPTS,
                    accountNumber,SecurityException.SecurityAction.LOGIN_ATTEMPT,
                    "Cont blocat pentru " + LOCK_DURATION_MINUTES + " minute");
        }
    }

    /**
     * reseteaza incercarile esuate pentru un cont
     */
    private void resetFailedAttempts(String accountNumber){
        failedLoginAttempts.remove(accountNumber);
        lockedAccounts.remove(accountNumber);
    }

    /**
     * deblocheaza manual un cont
     */
    public void unlockAccount(String accountNumber){
        failedLoginAttempts.remove(accountNumber);
        lockedAccounts.remove(accountNumber);
    }

    /**
     * returneaza numarul de incercari esuate pentru un cont
     */
    public int getFailedAttempts(String accountNumber){
        return failedLoginAttempts.getOrDefault(accountNumber,0);
    }

    /**
     * verifica daca un cont este blocat
     */
    public boolean isAccountLocked(String accountNumber){
        if (!lockedAccounts.containsKey(accountNumber)){
            return false;
        }

        LocalDateTime lockTime = lockedAccounts.get(accountNumber);
        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);

        if (LocalDateTime.now().isBefore(unlockTime)){
            return true;
        }
        else {
            //perioada de blocare a expirat
            lockedAccounts.remove(accountNumber);
            failedLoginAttempts.remove(accountNumber);
            return false;
        }
    }

    //Schimba parola
    /**
     * schimba parola unui client
     */
    public boolean changePassword(String accountNumber,String oldPassword,String newPassword){
        //autentifica cu parola veche
        Account account = authenticateClient(accountNumber,oldPassword);

        //validare parola noua
        ValidationService validationService = new ValidationService();
        validationService.validatePassword(newPassword);

        // In implementarea reala, este nevoie de a salva hash-ul noii parole în Account
        // Pentru simplificare, doar returnam succes

        // Inregistreaza un  eveniment
        // Este nevoie de a crea o tranzactie pentru schimbarea parolei

        return true;
    }

    //Verificari autorizare
    /**
     * verifica daca un client are acces la un cont
     */
    public boolean hasAccessToAccount(String accountNumber, Customer customer){
        Account account = accountService.findAccount(accountNumber);

        //verifica doar daca customerul este proprietar
        //in practica este nevoie de o relatie Customer si Account
        return account.getOwner().equals(customer);
    }

}

























