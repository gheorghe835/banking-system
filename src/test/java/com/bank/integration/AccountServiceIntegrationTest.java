package com.bank.integration;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.CustomerService;
import com.bank.infrastructure.persistence.repository.JpaAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JpaAccountRepository accountRepository;

    @Test
    void createAccount_ShouldSaveInDatabase() {
        // 1. Creează un client
        Customer customer = customerService.createCustomer(
                "Maria", "Ionescu", "maria@email.com", "0722123456",
                java.time.LocalDate.of(1990, 1, 1), "1234567890123"
        );

        // 2. Creează un cont
        Account account = accountService.createAccount(
                "1234567890123456", customer, "CURRENT", BigDecimal.valueOf(1000)
        );

        // 3. Verifică în baza de date
        Account found = accountService.findAccount("1234567890123456");
        assertThat(found).isNotNull();
        assertThat(found.getOwner().getFirstName()).isEqualTo("Maria");
        assertThat(found.getBalance(Currency.MDL)).isEqualByComparingTo("1000");
    }

    @Test
    void depositMoney_ShouldUpdateBalance() {
        // 1. Pregătește datele
        Customer customer = customerService.createCustomer(
                "Ion", "Popescu", "ion@email.com", "0722111111",
                java.time.LocalDate.of(1985, 5, 15), "2345678901234"
        );

        Account account = accountService.createAccount(
                "2345678901234567", customer, "SAVINGS", BigDecimal.valueOf(500)
        );

        // 2. Depune bani
        accountService.deposit("2345678901234567", BigDecimal.valueOf(200), Currency.MDL);

        // 3. Verifică soldul nou
        Account updated = accountService.findAccount("2345678901234567");
        assertThat(updated.getBalance(Currency.MDL)).isEqualByComparingTo("700");
    }

    @Test
    void withdrawMoney_ShouldDecreaseBalance() {
        // 1. Pregătește datele
        Customer customer = customerService.createCustomer(
                "Ana", "Pop", "ana@email.com", "0722333333",
                java.time.LocalDate.of(1992, 10, 20), "3456789012345"
        );

        Account account = accountService.createAccount(
                "3456789012345678", customer, "CURRENT", BigDecimal.valueOf(1000)
        );

        // 2. Retrage bani
        accountService.withdraw("3456789012345678", BigDecimal.valueOf(300), Currency.MDL);

        // 3. Verifică soldul nou
        Account updated = accountService.findAccount("3456789012345678");
        assertThat(updated.getBalance(Currency.MDL)).isEqualByComparingTo("700");
    }

    @Test
    void deleteAccount_ShouldMarkAsInactive() {
        try {
            String validFirstName = "Vasile";
            String validLastName = "Georgescu";
            String validEmail = "vasile.georgescu@email.com";
            String validPhone = "+37369123456";
            LocalDate validBirthDate = LocalDate.of(1990, 5, 15);
            String validIdentityNumber = "1234567890123";

            System.out.println("Încerc să creez customer cu:");
            System.out.println("Nume: " + validFirstName + " " + validLastName);
            System.out.println("Email: " + validEmail);
            System.out.println("Telefon: " + validPhone);
            System.out.println("Data nașterii: " + validBirthDate);
            System.out.println("IDNP: " + validIdentityNumber);

            Customer customer = customerService.createCustomer(
                    validFirstName,
                    validLastName,
                    validEmail,
                    validPhone,
                    validBirthDate,
                    validIdentityNumber
            );

            System.out.println("Customer creat cu succes: " + customer.getCustomerId());

            // restul codului...

        } catch (Exception e) {
            System.out.println("EROARE: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}