package com.bank.integration;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerAccountIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Test
    void customer_CanHaveMultipleAccounts() {
        // 1. Creează un client
        Customer customer = customerService.createCustomer(
                "Andrei", "Voicu", "andrei@email.com", "0722999999",
                java.time.LocalDate.of(1986, 4, 30), "9012345678901"
        );

        // 2. Creează două conturi pentru același client
        accountService.createAccount("5555555555555555", customer, "CURRENT", BigDecimal.valueOf(1000));
        accountService.createAccount("6666666666666666", customer, "SAVINGS", BigDecimal.valueOf(5000));

        // 3. Găsește toate conturile clientului
        List<Account> accounts = accountService.getCustomerAccounts(customer.getCustomerId());

        // 4. Verifică
        assertThat(accounts).hasSize(2);
    }

    @Test
    void deactivateCustomer_ShouldNotDeleteAccounts() {
        // 1. Creează un client și un cont
        Customer customer = customerService.createCustomer(
                "Mihai", "Dumitrescu", "mihai@email.com", "0722000000",
                java.time.LocalDate.of(1989, 8, 5), "0123456789012"
        );

        accountService.createAccount("7777777777777777", customer, "CURRENT", BigDecimal.valueOf(2000));

        // 2. Dezactivează clientul
        customerService.deactivateCustomer(customer.getCustomerId());

        // 3. Verifică că contul există încă
        Account account = accountService.findAccount("7777777777777777");
        assertThat(account).isNotNull();
        assertThat(account.isActive()).isTrue(); // Contul rămâne activ
    }
}
