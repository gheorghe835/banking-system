package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.*;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Serviciu pentru autentificare și autorizare
 */
@Service
public class AuthService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final Map<String, Integer> failedLoginAttempts = new HashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new HashMap<>();

    // Constante pentru securitate
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 30;

    // Credențiale hardcodate pentru manager (pentru demo)
    private static final String MANAGER_USERNAME = "admin";
    private static final String MANAGER_PASSWORD = "Admin1234";

    public AuthService(AccountService accountService,
                       TransactionRepository transactionRepository,
                       AccountRepository accountRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // ===== AUTENTIFICARE CLIENT =====

    /**
     * Autentifică un client
     */
    public Account authenticateClient(String accountNumber, String password) {
        // Verifică dacă contul este blocat
        checkIfAccountLocked(accountNumber);

        try {
            // Găsește contul
            Account account = accountService.findAccount(accountNumber);

            if (account.getOwner() != null) {
                account.getOwner().getFullName(); // Forțează încărcarea
            }

            // Verifică dacă contul este activ
            if (!account.isActive()) {
                throw new BankingException(BankingErrorCode.ACCOUNT_INACTIVE,
                        "Contul este inactiv");
            }

            if(!password.equals(account.getPasswordHash())){
                handleFailedLoginAttempt(accountNumber);
                throw new BankingSecurityException(BankingErrorCode.INVALID_CREDENTIALS,
                        accountNumber,BankingSecurityException.SecurityAction.LOGIN_ATTEMPT);
            }

            // Reset failed attempts on successful login
            resetFailedAttempts(accountNumber);

            // Update last login time
            accountService.updateLastLogin(accountNumber);

            return account;

        } catch (AccountNotFoundException e) {
            // Treat non-existent account as failed login attempt
            handleFailedLoginAttempt(accountNumber);
            throw e;
        }
    }

    // ===== AUTENTIFICARE MANAGER =====

    /**
     * Autentifică un manager
     */
    public BankManager authenticateManager(String username, String password) {

        if (MANAGER_USERNAME.equals(username) && MANAGER_PASSWORD.equals(password)) {
            BankManager manager = createDemoManager();

            manager.setPasswordHash(MANAGER_PASSWORD);

            return manager;
        }

        throw new BankingSecurityException(BankingErrorCode.INVALID_CREDENTIALS,
                username, BankingSecurityException.SecurityAction.LOGIN_ATTEMPT);
    }

    private BankManager createDemoManager() {
        BankManager manager = new BankManager(
                MANAGER_USERNAME,
                "Admin",
                "System",
                "admin@bank.com",
                BankManager.AccessLevel.ADMIN
        );

        manager.setPasswordHash(MANAGER_PASSWORD);

        return manager;
    }

    // ===== GESTIUNE BLOCHARE CONTURI =====

    /**
     * Verifică dacă un cont este blocat
     */
    private void checkIfAccountLocked(String accountNumber) {
        if (lockedAccounts.containsKey(accountNumber)) {
            LocalDateTime lockTime = lockedAccounts.get(accountNumber);
            LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);

            if (LocalDateTime.now().isBefore(unlockTime)) {
                long minutesRemaining = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
                throw new BankingSecurityException(BankingErrorCode.ACCOUNT_BLOCKED,
                        accountNumber, BankingSecurityException.SecurityAction.LOGIN_ATTEMPT,
                        "Cont blocat. Încercați din nou peste " + minutesRemaining + " minute");
            } else {
                // Lock period has expired
                lockedAccounts.remove(accountNumber);
                failedLoginAttempts.remove(accountNumber);
            }
        }
    }

    /**
     * Gestionează o încercare de login eșuată
     */

    private void handleFailedLoginAttempt(String accountNumber) {
        int attempts = failedLoginAttempts.getOrDefault(accountNumber, 0) + 1;
        failedLoginAttempts.put(accountNumber, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            // Blochează contul
            lockedAccounts.put(accountNumber, LocalDateTime.now());
            throw new BankingSecurityException(BankingErrorCode.TOO_MANY_ATTEMPTS,
                    accountNumber, BankingSecurityException.SecurityAction.LOGIN_ATTEMPT,
                    "Cont blocat pentru " + LOCK_DURATION_MINUTES + " minute" +
                            "Prea multe încercări");
        }
    }

    /**
     * Resetează încercările eșuate pentru un cont
     */
    private void resetFailedAttempts(String accountNumber) {
        failedLoginAttempts.remove(accountNumber);
        lockedAccounts.remove(accountNumber);
    }

    /**
     * Deblochează manual un cont
     */
    public void unlockAccount(String accountNumber) {
        failedLoginAttempts.remove(accountNumber);
        lockedAccounts.remove(accountNumber);
    }

    /**
     * Returnează numărul de încercări eșuate pentru un cont
     */
    public int getFailedAttempts(String accountNumber) {
        return failedLoginAttempts.getOrDefault(accountNumber, 0);
    }

    /**
     * Verifică dacă un cont este blocat
     */
    public boolean isAccountLocked(String accountNumber) {
        if (!lockedAccounts.containsKey(accountNumber)) {
            return false;
        }

        LocalDateTime lockTime = lockedAccounts.get(accountNumber);
        LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);

        if (LocalDateTime.now().isBefore(unlockTime)) {
            return true;
        } else {
            // Lock period has expired
            lockedAccounts.remove(accountNumber);
            failedLoginAttempts.remove(accountNumber);
            return false;
        }
    }

    // ===== SCHIMBARE PAROLĂ =====

    /**
     * Schimbă parola unui cont
     */
    public boolean changePassword(String accountNumber,
                                  String oldPassword,
                                  String newPassword) {
        // Autentifică cu parola veche
        Account account = authenticateClient(accountNumber, oldPassword);

        // Validare parolă nouă
        ValidationService validationService = new ValidationService();
        validationService.validatePassword(newPassword);

        account.setPasswordHash(newPassword);
        accountRepository.save(account);


        // Înregistrează evenimentul
        Transaction transaction = new Transaction(
                Transaction.TransactionType.PASSWORD_CHANGE,
                BigDecimal.ZERO,
                Currency.MDL,
                "Schimbare parolă"
        );
        transaction.setSourceAccountNumber(accountNumber);
        transaction.markAsCompleted();
        transactionRepository.save(transaction);

        return true;
    }

    // ===== VERIFICĂRI AUTORIZARE =====

    /**
     * Verifică dacă un client are acces la un cont
     */
    public boolean hasAccessToAccount(String accountNumber, Customer customer) {
        Account account = accountService.findAccount(accountNumber);

        return account.getOwner().equals(customer);
    }

    public BankManager findManagerByUsername(String username) {
        if (MANAGER_USERNAME.equals(username)) {
            return createDemoManager();
        }
        throw new BankingSecurityException(BankingErrorCode.MANAGER_NOT_FOUND,
                username, BankingSecurityException.SecurityAction.ACCOUNT_ACCESS);
    }
}
