package com.bank.infrastructure.persistence.mapper;

import com.bank.domain.model.Account;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Currency;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class AccountMapper {

    public AccountEntity toEntity(Account account) {
        if (account == null) return null;

        // Owner
        CustomerEntity ownerEntity = new CustomerEntity();
        if (account.getOwner() != null) {
            ownerEntity.setCustomerId(account.getOwner().getCustomerId());
            ownerEntity.setFirstName(account.getOwner().getFirstName());
            ownerEntity.setLastName(account.getOwner().getLastName());
            ownerEntity.setEmail(account.getOwner().getEmail());
        }

        // Creează entitate
        AccountEntity entity = new AccountEntity();
        entity.setAccountNumber(account.getAccountNumber());
        entity.setOwner(ownerEntity);
        entity.setAccountType(account.getAccountType());

        // Balanțe - folosește getAllBalances() care există
        Map<Currency, BigDecimal> balances = account.getAllBalances();
        entity.setBalanceMDL(balances.getOrDefault(Currency.MDL, BigDecimal.ZERO));
        entity.setBalanceEUR(balances.getOrDefault(Currency.EUR, BigDecimal.ZERO));
        entity.setBalanceUSD(balances.getOrDefault(Currency.USD, BigDecimal.ZERO));
        entity.setBalanceGBP(balances.getOrDefault(Currency.GBP, BigDecimal.ZERO));
        entity.setBalanceRON(balances.getOrDefault(Currency.RON, BigDecimal.ZERO));

        // Active
        entity.setActive(account.isActive());

        // Date implicite (pentru că Account nu are gettere pentru astea)
        entity.setCreationDate(LocalDate.now());
        entity.setLastResetDate(LocalDate.now());

        // Încearcă metode opționale cu try-catch
        try {
            entity.setLastLogin(account.getLastLogin());
        } catch (Exception e) {
            // Ignoră dacă nu există
        }

        try {
            entity.setDailyWithdrawalLimit(account.getDailyWithdrawalLimit());
        } catch (Exception e) {
            entity.setDailyWithdrawalLimit(new BigDecimal("5000.00"));
        }

        try {
            entity.setDailyWithdrawalUsed(account.getDailyWithdrawalUsed());
        } catch (Exception e) {
            entity.setDailyWithdrawalUsed(BigDecimal.ZERO);
        }

        return entity;
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) return null;

        // Owner
        Customer owner = new Customer();
        if (entity.getOwner() != null) {
            owner.setCustomerId(entity.getOwner().getCustomerId());
            owner.setFirstName(entity.getOwner().getFirstName());
            owner.setLastName(entity.getOwner().getLastName());
            owner.setEmail(entity.getOwner().getEmail());
            owner.setPhoneNumber(entity.getOwner().getPhoneNumber());
        }

        // Creează Account cu constructorul disponibil
        Account account = new Account(
                entity.getAccountNumber(),
                owner,
                entity.getAccountType(),
                entity.getBalanceMDL()
        );

        // Setează active
        account.setActive(entity.isActive());

        // Încearcă metode opționale
        try {
            account.setLastLogin(entity.getLastLogin());
        } catch (Exception e) {
            // Ignoră
        }

        try {
            account.setDailyWithdrawalLimit(entity.getDailyWithdrawalLimit());
        } catch (Exception e) {
            // Ignoră
        }

        try {
            account.setDailyWithdrawalUsed(entity.getDailyWithdrawalUsed());
        } catch (Exception e) {
            // Ignoră
        }

        // Adaugă alte balanțe
        if (entity.getBalanceEUR().compareTo(BigDecimal.ZERO) > 0) {
            account.deposit(entity.getBalanceEUR(), Currency.EUR);
        }
        if (entity.getBalanceUSD().compareTo(BigDecimal.ZERO) > 0) {
            account.deposit(entity.getBalanceUSD(), Currency.USD);
        }
        if (entity.getBalanceGBP().compareTo(BigDecimal.ZERO) > 0) {
            account.deposit(entity.getBalanceGBP(), Currency.GBP);
        }
        if (entity.getBalanceRON().compareTo(BigDecimal.ZERO) > 0) {
            account.deposit(entity.getBalanceRON(), Currency.RON);
        }

        return account;
    }
}
