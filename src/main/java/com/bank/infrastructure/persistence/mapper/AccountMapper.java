package com.bank.infrastructure.persistence.mapper;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccountMapper {

    public AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setAccountNumber(account.getAccountNumber());
        entity.setAccountType(account.getAccountType());
        entity.setCreationDate(account.getCreationDate());
        entity.setActive(account.isActive());
        entity.setDailyWithdrawalLimit(account.getDailyWithdrawalLimit());
        entity.setDailyWithdrawalUsed(account.getDailyWithdrawalUsed());
        entity.setPasswordHash(account.getPasswordHash());

        // Setează solduri
        entity.setBalanceMDL(account.getBalance(Currency.MDL));
        entity.setBalanceEUR(account.getBalance(Currency.EUR));
        entity.setBalanceUSD(account.getBalance(Currency.USD));
        entity.setBalanceGBP(account.getBalance(Currency.GBP));
        entity.setBalanceRON(account.getBalance(Currency.RON));

        // Mapează owner
        if (account.getOwner() != null) {
            CustomerEntity customerEntity = new CustomerEntity();
            customerEntity.setCustomerId(account.getOwner().getCustomerId());
            customerEntity.setFirstName(account.getOwner().getFirstName());
            customerEntity.setLastName(account.getOwner().getLastName());
            customerEntity.setEmail(account.getOwner().getEmail());
            entity.setOwner(customerEntity);
        }

        return entity;
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        // Construiește Customer
        Customer customer = null;
        if (entity.getOwner() != null) {
            customer = new Customer();
            customer.setCustomerId(entity.getOwner().getCustomerId());
            customer.setFirstName(entity.getOwner().getFirstName());
            customer.setLastName(entity.getOwner().getLastName());
            customer.setEmail(entity.getOwner().getEmail());
        }

        // Construiește Account
        Account account = new Account();
        account.setAccountNumber(entity.getAccountNumber());
        account.setAccountType(entity.getAccountType());
        account.setCreationDate(entity.getCreationDate());
        account.setActive(entity.isActive());
        account.setDailyWithdrawalLimit(entity.getDailyWithdrawalLimit());
        account.setDailyWithdrawalUsed(entity.getDailyWithdrawalUsed());
        account.setLastResetDate(entity.getLastResetDate());
        account.setPasswordHash(entity.getPasswordHash());
        account.setOwner(customer);

        //  Construiește deposit
        account.setBalance(Currency.MDL, entity.getBalanceMDL() != null ? entity.getBalanceMDL() : BigDecimal.ZERO);
        account.setBalance(Currency.EUR, entity.getBalanceEUR() != null ? entity.getBalanceEUR() : BigDecimal.ZERO);
        account.setBalance(Currency.USD, entity.getBalanceUSD() != null ? entity.getBalanceUSD() : BigDecimal.ZERO);
        account.setBalance(Currency.GBP, entity.getBalanceGBP() != null ? entity.getBalanceGBP() : BigDecimal.ZERO);
        account.setBalance(Currency.RON, entity.getBalanceRON() != null ? entity.getBalanceRON() : BigDecimal.ZERO);

        return account;
    }
}