package com.bank.integration;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Test
    void transferMoney_BetweenTwoAccounts_ShouldUpdateBothBalances() {
        // Creează clienți
        Customer sender = customerService.createCustomer(
                "Emil", "Preda", "emil@email.com", "0722555555",
                LocalDate.of(1991, 7, 12), "5678901234567"
        );

        Customer receiver = customerService.createCustomer(
                "Elena", "Dinu", "elena@email.com", "0722666666",
                LocalDate.of(1993, 9, 25), "6789012345678"
        );

        // Creează conturi cu solduri
        accountService.createAccount("1111111111111111", sender, "CURRENT", BigDecimal.valueOf(2000));
        accountService.createAccount("2222222222222222", receiver, "SAVINGS", BigDecimal.valueOf(500));

        // Transferă
        accountService.transfer("1111111111111111", "2222222222222222",
                BigDecimal.valueOf(300), Currency.MDL, "Test");

        // Verifică
        Account from = accountService.findAccount("1111111111111111");
        Account to = accountService.findAccount("2222222222222222");

        assertThat(from.getBalance(Currency.MDL)).isEqualByComparingTo("1700");
        assertThat(to.getBalance(Currency.MDL)).isEqualByComparingTo("800");
    }

    @Test
    void transferMoney_InsufficientFunds_ShouldThrowException() {
        // Creează clienți
        Customer sender = customerService.createCustomer(
                "Radu", "Stanciu", "radu@email.com", "0722777777",
                LocalDate.of(1987, 11, 3), "7890123456789"
        );

        Customer receiver = customerService.createCustomer(
                "Oana", "Munteanu", "oana@email.com", "0722888888",
                LocalDate.of(1994, 2, 18), "8901234567890"
        );

        // Creează conturi - unul cu sold mic
        accountService.createAccount("3333333333333333", sender, "CURRENT", BigDecimal.valueOf(100));
        accountService.createAccount("4444444444444444", receiver, "SAVINGS", BigDecimal.valueOf(50));

        // Verifică că aruncă excepție
        assertThatThrownBy(() ->
                accountService.transfer("3333333333333333", "4444444444444444",
                        BigDecimal.valueOf(500), Currency.MDL, "Imposibil")
        ).isInstanceOf(InsufficientFundsException.class);

        // Verifică soldurile neschimbate
        Account from = accountService.findAccount("3333333333333333");
        Account to = accountService.findAccount("4444444444444444");

        assertThat(from.getBalance(Currency.MDL)).isEqualByComparingTo("100");
        assertThat(to.getBalance(Currency.MDL)).isEqualByComparingTo("50");
    }
}
