package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * clasa care reprezinta un cont bancar
 * gestioneaza soldurile in multiple valute si operatiunile de baza
 */

public class Account {
    private String accountNumber;//numar cont - 16 cifre
    private Customer owner; //proprietarul contului
    private Map<Currency, BigDecimal> balances; //so;duri pe valute
    private String accountType; //tip de cont - CURRENT, SAVINGS, etc.
    private LocalDate creationDate;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private BigDecimal dailyWithdrawalLimit; // limita zilnica in MDL
    private BigDecimal dailyWithdrawalUsed; //suma utilizata astazi
    private LocalDate lastResetDate; //ultima resetare limita

    //constante
    public static final int ACCOUNT_NUMBER_LENGTH = 16;
    public static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(10);
    public static final BigDecimal MINIMUM_DEPOSIT = BigDecimal.valueOf(1);
    public static final BigDecimal DEFAULT_DAILY_LIMIT = BigDecimal.valueOf(5000);

    //tipuri de conturi
    public static final String ACCOUNT_TYPE_CURRENT = "CURRENT";
    public static final String ACCOUNT_TYPE_SAVINGS = "SAVINGS";
    public static final String ACCOUNT_TYPE_BUISINESS = "BUISINESS";

    //constructori
    public Account(){
        this.balances = new HashMap<>();
        this.creationDate = LocalDate.now();
        this.isActive = true;
        this.dailyWithdrawalLimit = DEFAULT_DAILY_LIMIT;
        this.dailyWithdrawalUsed = BigDecimal.ZERO;
        this.lastResetDate = LocalDate.now();
        initializeBalances();
    }
    public Account(String accountNumber,Customer owner,String accountType){
        this();
        setAccountNumber(accountNumber);
        this.owner = Objects.requireNonNull(owner,"Proprietarul nu poate fi null");
        setAccountType(accountType);
    }
    public Account(String accountNumber,Customer owner,String accountType,BigDecimal initialBalance){
        this(accountNumber,owner,accountType);
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Soldul initial nu poate fi negativ.");
        }
        deposit(initialBalance,Currency.MDL);
    }

    //metode de initializare
    private void initializeBalances(){
        for (Currency currency : Currency.values()){
            balances.put(currency,BigDecimal.ZERO);
        }
    }

    //validari
    private void validateAccountNumber(String accountNumber){
        if (accountNumber == null || accountNumber.length() != ACCOUNT_NUMBER_LENGTH){
            throw new IllegalArgumentException(
                    String.format("Numarul contului trebuie sa aiba %d cifre",ACCOUNT_NUMBER_LENGTH)
            );
        }
        if (!accountNumber.matches("\\d+")){
            throw new IllegalArgumentException("Numarul contului trebuie sa contina cifre");
        }
    }

    //getteri si setteri
    public String getAccountNumber(){return accountNumber;}
    public void setAccountNumber(String accountNumber){
        validateAccountNumber(accountNumber);
        this.accountNumber = accountNumber;
    }
    public Customer getOwner(){return owner;}
    public void setOwner(Customer owner){this.owner = Objects.requireNonNull(owner,"Proprietarul nu poate fi null");}
    public String getAccountType(){return accountType;}
    public void setAccountType(String accountType){
        if (!isValidAccountType(accountType)){
            throw new IllegalArgumentException("Tip de cont invalid: " + accountType);
        }
        this.accountType = accountType;
    }
    private boolean isValidAccountType(String type){
        return ACCOUNT_TYPE_CURRENT.equals(type) ||
                ACCOUNT_TYPE_SAVINGS.equals(type) ||
                ACCOUNT_TYPE_BUISINESS.equals(type);
    }
    public LocalDate getCreationDate(){return creationDate;}
    public LocalDateTime getLastLogin(){return lastLogin;}
    public void setLastLogin(LocalDateTime lastLogin){this.lastLogin = lastLogin;}
    public void updateLastLogin(){this.lastLogin = LocalDateTime.now();}
    public boolean isActive(){return isActive;}
    public void setActive(boolean active){isActive = active;}
    public void deactivate(){this.isActive = false;}
    public void activate(){this.isActive = true;}
    public BigDecimal getDailyWithdrawalLimit(){return dailyWithdrawalLimit;}
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit){
        if (dailyWithdrawalLimit.compareTo(BigDecimal.valueOf(100)) < 0){
            throw new IllegalArgumentException("Limita zilnica trebuie sa fie minim 100 MDL");
        }
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }
    public BigDecimal getDailyWithdrawalUsed(){
        resetDailyLimitIfNeeded();
        return dailyWithdrawalUsed;
    }


    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().length() < 2) {
            throw new IllegalArgumentException("Numele trebuie să aibă minim 2 caractere");
        }

        // Actualizează numele clientului
        if (this.owner != null) {

            String[] names = ownerName.trim().split(" ", 2);
            if (names.length >= 2) {
                this.owner.setFirstName(names[0]);
                this.owner.setLastName(names[1]);
            } else {
                this.owner.setFirstName(ownerName);
                this.owner.setLastName("");
            }
        } else {

            this.owner = new Customer(ownerName, "", "", "", null, "");
        }
    }

    //resetare limita zilnica
    private void resetDailyLimitIfNeeded(){
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate)){
            dailyWithdrawalUsed = BigDecimal.ZERO;
            lastResetDate = today;
        }
    }

    //operatiuni cu solduri
    public BigDecimal getBalance(Currency currency){
        return balances.getOrDefault(currency,BigDecimal.ZERO);
    }
    public Map<Currency,BigDecimal> getAllBalances(){
        return new HashMap<>(balances);
    }
    public BigDecimal getTotalBalancesInMDL(){
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Currency,BigDecimal> entry : balances.entrySet()){
            BigDecimal amountInMDL = entry.getValue()
                    .multiply(BigDecimal.valueOf(entry.getKey().getExchangeRateToMDL()));
            total = total.add(amountInMDL);
        }
        return total;
    }

    //operatiuni bancare
    public boolean deposit(BigDecimal amount,Currency currency){
        if (!isActive){
            throw new IllegalArgumentException("Contul este inactiv.");
        }
        if (amount.compareTo(MINIMUM_DEPOSIT) < 0){
            throw new IllegalArgumentException(
                    String.format("Suma minima pentru depunere este %s%s",MINIMUM_DEPOSIT,currency)
            );
        }
        BigDecimal currentBalance = getBalance(currency);
        BigDecimal newBalance = currentBalance.add(amount);
        balances.put(currency,newBalance);
        return true;
    }
    public boolean withdraw(BigDecimal amount,Currency currency){
        if (!isActive){
            throw new IllegalArgumentException("Contul este inactiv.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        }
        resetDailyLimitIfNeeded();
        //verifica limita zilnica
        BigDecimal amountInMDL = currency == Currency.MDL ?
                amount :
                amount.multiply(BigDecimal.valueOf(currency.getExchangeRateToMDL()));

        BigDecimal remainingLimit = dailyWithdrawalLimit.subtract(dailyWithdrawalUsed);
        if (amountInMDL.compareTo(remainingLimit) > 0){
            throw new IllegalArgumentException(
                    String.format("Limita zilnica depasita. Disponibil: %s MDL",remainingLimit)
            );
        }
        //verifica soldul
        BigDecimal currentBalance = getBalance(currency);
        if (currentBalance.compareTo(amount) < 0){
            throw new IllegalArgumentException(
                    String.format("Fonduri insuficiente. Disponibil:%s%s",currentBalance,currency)
            );
        }
        //efectuiaza retragerea
        BigDecimal newBalance = currentBalance.subtract(amount);
        balances.put(currency,newBalance);
        dailyWithdrawalUsed = dailyWithdrawalUsed.add(amountInMDL);
        return true;
    }
    public boolean hasSufficientFounds(BigDecimal amount,Currency currency){
        return getBalance(currency).compareTo(amount) >= 0;
    }


    //metode utilitare
    public int getAccountAgeInDays(){
        return (int) ChronoUnit.DAYS.between(creationDate,LocalDate.now());
    }

    public boolean transferTo(Account targetAccount, BigDecimal amount,
                              Currency currency, String description) {
        // 1. Validări
        if (targetAccount == null) {
            throw new IllegalArgumentException("Contul destinație nu poate fi null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Suma transferului trebuie să fie pozitivă");
        }

        // 2. Verifică starea conturilor
        if (!this.isActive() || !targetAccount.isActive()) {
            throw new IllegalStateException("Unul dintre conturi este inactiv");
        }

        // 3. Verifică dacă nu e același cont
        if (this.equals(targetAccount)) {
            throw new IllegalArgumentException("Nu puteți transfera către același cont");
        }

        // 4. Efectuează transferul
        try {
            // Retrage din contul curent
            this.withdraw(amount, currency);

            // Depune în contul destinație
            targetAccount.deposit(amount, currency);

            return true;

        } catch (Exception e) {
            try {
                this.deposit(amount, currency); // Readuce banii
            }
            catch (Exception rollbackEx) {
            }
            throw e; // Aruncă eroarea originală
        }
    }
    //override metode
    @Override
    public boolean equals(Object o){
        if (this == o)return true;
        if (o == null || getClass() != o.getClass())return false;
        Account account =(Account) o;
        return Objects.equals(accountNumber,account.accountNumber);
    }

    @Override
    public int hashCode(){return Objects.hash(accountNumber);}

    @Override
    public String toString(){
        return String.format("Account[Number =%s, Owner =%s, Type =%s, Type =%s, Active =%s, Total =%.2f MDL]",
                accountNumber,
                owner != null ? owner.getFullName() : "N/A",
                accountType,
                isActive,
                getTotalBalancesInMDL());
    }
}
