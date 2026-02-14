package com.bank.application.web.webMapper;

import com.bank.application.web.dto.AccountDTO;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebAccountMapper {
    /**
     * Convertește un obiect Account (domain) în AccountDTO (pentru API)
     */
    public AccountDTO toDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountDTO dto = new AccountDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setOwnerName(account.getOwner() != null ? account.getOwner().getFullName() : null);
        dto.setOwnerId(account.getOwner() != null ? account.getOwner().getCustomerId() : null);
        dto.setAccountType(account.getAccountType());
        dto.setCreationDate(account.getCreationDate());
        dto.setActive(account.isActive());
        dto.setDailyWithdrawalLimit(account.getDailyWithdrawalLimit());

        // Convertește soldurile din Map<Currency, BigDecimal> în Map<String, BigDecimal>
        Map<String, BigDecimal> balancesMap = new HashMap<>();
        for (Currency currency : Currency.values()) {
            BigDecimal balance = account.getBalance(currency);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                balancesMap.put(currency.getCode(), balance);
            }
        }
        dto.setBalances(balancesMap);

        return dto;
    }

    /**
     * Convertește un AccountDTO în Account (domain)
     * (mai rar folosit, de obicei doar pentru creare)
     */
    public Account toDomain(AccountDTO dto) {
        if (dto == null) {
            return null;
        }

        // Notă: Această metodă este mai complexă pentru că Account are nevoie de Customer
        // De obicei nu convertim direct din DTO în Domain, ci folosim service-ul
        // Aceasta este doar o schiță - în practică vei folosi createAccount din service

        Account account = new Account();
        account.setAccountNumber(dto.getAccountNumber());
        account.setAccountType(dto.getAccountType());
        account.setCreationDate(dto.getCreationDate());
        account.setActive(dto.isActive());
        account.setDailyWithdrawalLimit(dto.getDailyWithdrawalLimit());

        // Owner-ul trebuie setat separat din baza de date

        return account;
    }

    /**
     * Versiune simplificată pentru răspunsuri rapide (fără solduri detaliate)
     */
    public AccountDTO toSimpleDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountDTO dto = new AccountDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setOwnerName(account.getOwner() != null ? account.getOwner().getFullName() : null);
        dto.setAccountType(account.getAccountType());
        dto.setActive(account.isActive());

        // Doar soldul total în MDL
        Map<String, BigDecimal> simpleBalance = new HashMap<>();
        simpleBalance.put("TOTAL_MDL", account.getTotalBalanceInMDL());
        dto.setBalances(simpleBalance);

        return dto;
    }
}
