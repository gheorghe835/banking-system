package com.bank.domain.model;

import com.bank.domain.exception.InsufficientFundsException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Clasa care reprezintă un cont bancar
 * Gestionează soldurile în multiple valute și operațiunile de bază
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accountNumber;           // Număr cont (16 cifre)
    private Customer owner;                 // Proprietarul contului
    private Map<Currency, BigDecimal> balances; // Solduri pe valute
    private String accountType;
    private String passwordHash;// Tip cont: CURRENT, SAVINGS, etc.
    private LocalDate creationDate;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private BigDecimal dailyWithdrawalLimit; // Limita zilnică în MDL
    private BigDecimal dailyWithdrawalUsed;  // Suma utilizată astăzi
    private LocalDate lastResetDate;        // Ultima resetare limită

    // Constante
    public static final int ACCOUNT_NUMBER_LENGTH = 16;
    public static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(10);
    public static final BigDecimal MINIMUM_DEPOSIT = BigDecimal.valueOf(1);
    public static final BigDecimal DEFAULT_DAILY_LIMIT = BigDecimal.valueOf(5000);

    // Tipuri de cont
    public static final String ACCOUNT_TYPE_CURRENT = "CURRENT";
    public static final String ACCOUNT_TYPE_SAVINGS = "SAVINGS";
    public static final String ACCOUNT_TYPE_BUSINESS = "BUSINESS";

    // Constructori
    public Account() {
        this.balances = new HashMap<>();
        this.creationDate = LocalDate.now();
        this.isActive = true;
        this.dailyWithdrawalLimit = DEFAULT_DAILY_LIMIT;
        this.dailyWithdrawalUsed = BigDecimal.ZERO;
        this.lastResetDate = LocalDate.now();
        initializeBalances();
    }

    public Account(String accountNumber, Customer owner, String accountType) {
        this();
        setAccountNumber(accountNumber);
        this.owner = Objects.requireNonNull(owner, "Proprietarul nu poate fi null");
        setAccountType(accountType);
    }

    public Account(String accountNumber, Customer owner, String accountType,
                   BigDecimal initialBalance) {
        this(accountNumber, owner, accountType);
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Soldul inițial nu poate fi negativ");
        }
        deposit(initialBalance, Currency.MDL);
        this.passwordHash = "Parola1234";
    }

    public Account(String accountNumber, Customer owner, String accountType,
                   BigDecimal initialBalance, String password) {  // ← PAROLĂ NOUĂ
        this(accountNumber, owner, accountType, initialBalance);
        this.passwordHash = password;  // În realitate, aici ar fi hash-uit
    }

    // Metode de inițializare
    private void initializeBalances() {
        for (Currency currency : Currency.values()) {
            balances.put(currency, BigDecimal.ZERO);
        }
    }

    // Validări
    private void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() != ACCOUNT_NUMBER_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Numărul contului trebuie să aibă %d cifre", ACCOUNT_NUMBER_LENGTH));
        }
        if (!accountNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Numărul contului trebuie să conțină doar cifre");
        }
    }

    // Getteri și Setteri
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setDailyWithdrawalUsed(BigDecimal dailyWithdrawalUsed) {
        this.dailyWithdrawalUsed = dailyWithdrawalUsed;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setLastResetDate(LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public void setAccountNumber(String accountNumber) {
        validateAccountNumber(accountNumber);
        this.accountNumber = accountNumber;
    }

    public void setBalance(Currency currency, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Suma nu poate fi negativă");
        }
        this.balances.put(currency, amount);
    }
    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = Objects.requireNonNull(owner, "Proprietarul nu poate fi null");
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        if (!isValidAccountType(accountType)) {
            throw new IllegalArgumentException("Tip de cont invalid: " + accountType);
        }
        this.accountType = accountType;
    }

    private boolean isValidAccountType(String type) {
        return ACCOUNT_TYPE_CURRENT.equals(type) ||
                ACCOUNT_TYPE_SAVINGS.equals(type) ||
                ACCOUNT_TYPE_BUSINESS.equals(type);
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public BigDecimal getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) {
        if (dailyWithdrawalLimit.compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new IllegalArgumentException("Limita zilnică trebuie să fie minim 100 MDL");
        }
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public BigDecimal getDailyWithdrawalUsed() {
        resetDailyLimitIfNeeded();
        return dailyWithdrawalUsed;
    }

    // Resetare limită zilnică
    private void resetDailyLimitIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate)) {
            dailyWithdrawalUsed = BigDecimal.ZERO;
            lastResetDate = today;
        }
    }

    // Operațiuni cu solduri
    public BigDecimal getBalance(Currency currency) {
        return balances.getOrDefault(currency, BigDecimal.ZERO);
    }

    public Map<Currency, BigDecimal> getAllBalances() {
        return new HashMap<>(balances);
    }

    public BigDecimal getTotalBalanceInMDL() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Currency, BigDecimal> entry : balances.entrySet()) {
            BigDecimal amountInMDL = entry.getValue()
                    .multiply(BigDecimal.valueOf(entry.getKey().getExchangeRateToMDL()));
            total = total.add(amountInMDL);
        }
        return total;
    }

    // Operațiuni bancare
    public boolean deposit(BigDecimal amount, Currency currency) {
        if (!isActive) {
            throw new IllegalStateException("Contul este inactiv");
        }
        if (amount.compareTo(MINIMUM_DEPOSIT) < 0) {
            throw new IllegalArgumentException(
                    String.format("Suma minimă pentru depunere este %s %s",
                            MINIMUM_DEPOSIT, currency));
        }

        BigDecimal currentBalance = getBalance(currency);
        BigDecimal newBalance = currentBalance.add(amount);
        balances.put(currency, newBalance);
        return true;
    }

    public boolean withdraw(BigDecimal amount, Currency currency) {
        if (!isActive) {
            throw new IllegalStateException("Contul este inactiv");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Suma trebuie să fie pozitivă");
        }

        resetDailyLimitIfNeeded();

        // Verifică limita zilnică
        BigDecimal amountInMDL = currency == Currency.MDL ?
                amount :
                amount.multiply(BigDecimal.valueOf(currency.getExchangeRateToMDL()));

        BigDecimal remainingLimit = dailyWithdrawalLimit.subtract(dailyWithdrawalUsed);
        if (amountInMDL.compareTo(remainingLimit) > 0) {
            throw new IllegalArgumentException(
                    String.format("Limita zilnică depășită. Disponibil: %s MDL", remainingLimit));
        }

        // Verifică soldul
        BigDecimal currentBalance = getBalance(currency);
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    accountNumber,
                    amount,
                    currentBalance,
                    currency);
        }

        // Efectuează retragerea
        BigDecimal newBalance = currentBalance.subtract(amount);
        balances.put(currency, newBalance);
        dailyWithdrawalUsed = dailyWithdrawalUsed.add(amountInMDL);
        return true;
    }

    public boolean hasSufficientFunds(BigDecimal amount, Currency currency) {
        return getBalance(currency).compareTo(amount) >= 0;
    }

    // Metode utilitare
    public int getAccountAgeInDays() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(creationDate, LocalDate.now());
    }

    public void setBalancesFromMap(Map<Currency, BigDecimal> balancesMap) {
        for (Map.Entry<Currency, BigDecimal> entry : balancesMap.entrySet()) {
            this.balances.put(entry.getKey(), entry.getValue());
        }
    }
    public void clearBalances() {
        for (Currency currency : Currency.values()) {
            balances.put(currency, BigDecimal.ZERO);
        }
    }

    // Override metode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return String.format("Account[Number=%s, Owner=%s, Type=%s, Active=%s, Total=%.2f MDL]",
                accountNumber,
                owner != null ? owner.getFullName() : "N/A",
                accountType,
                isActive,
                getTotalBalanceInMDL());
    }
}