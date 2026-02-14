package com.bank.integration;

import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import com.bank.infrastructure.persistence.entity.TransactionEntity;
import com.bank.infrastructure.persistence.repository.JpaTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaTransactionRepository transactionRepository;

    private AccountEntity testAccount;
    private final String ACCOUNT_NUMBER = "1234567890123456";
    private final String TRANSACTION_ID = "TXN001";

    @BeforeEach
    void setUp() {
        // Creează customer
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId("CUST001");
        customer.setFirstName("Ion");
        customer.setLastName("Popescu");
        customer.setEmail("ion.popescu@email.com");
        customer.setPhoneNumber("069123456");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setIdentityNumber("1234567890123");
        customer.setRegistrationDate(LocalDateTime.now());
        customer.setActive(true);
        entityManager.persistAndFlush(customer);

        // Creează cont
        testAccount = new AccountEntity();
        testAccount.setAccountNumber(ACCOUNT_NUMBER);
        testAccount.setOwner(customer);
        testAccount.setAccountType("CURRENT");
        testAccount.setBalanceMDL(BigDecimal.valueOf(5000));
        testAccount.setCreationDate(LocalDate.now());
        testAccount.setActive(true);
        testAccount.setDailyWithdrawalLimit(BigDecimal.valueOf(5000));
        testAccount.setDailyWithdrawalUsed(BigDecimal.ZERO);
        testAccount.setLastResetDate(LocalDate.now());
        entityManager.persistAndFlush(testAccount);

        // Creează tranzacție
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionId(TRANSACTION_ID);
        transaction.setAccount(testAccount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setCurrency("MDL");
        transaction.setDescription("Test deposit");
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("COMPLETED");
        transaction.setTargetAccount(ACCOUNT_NUMBER);

        entityManager.persistAndFlush(transaction);
        entityManager.clear();
    }

    @Test
    void sumAmountByAccountAndTypes_ShouldReturnTotalForDeposits() {
        BigDecimal sum = transactionRepository.sumAmountByAccountAndTypes(
                ACCOUNT_NUMBER, List.of("DEPOSIT"));

        // Folosește isEqualByComparingTo în loc de isEqualTo
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    // Restul testelor rămân la fel...
}