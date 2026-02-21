package com.bank.integration;

import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import com.bank.infrastructure.persistence.repository.JpaAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaAccountRepository accountRepository;

    private CustomerEntity testCustomer;
    private AccountEntity testAccount;
    private final String ACCOUNT_NUMBER = "1234567890123456";

    @BeforeEach
    void setUp() {
        // Creează un customer de test
        testCustomer = new CustomerEntity();
        testCustomer.setCustomerId("CUST001");
        testCustomer.setFirstName("Ion");
        testCustomer.setLastName("Popescu");
        testCustomer.setEmail("ion.popescu@email.com");
        testCustomer.setPhoneNumber("069123456");
        testCustomer.setBirthDate(LocalDate.of(1990, 1, 1));
        testCustomer.setIdentityNumber("1234567890123");
        testCustomer.setRegistrationDate(LocalDateTime.now());
        testCustomer.setActive(true);

        entityManager.persistAndFlush(testCustomer);

        // Creează un cont de test
        testAccount = new AccountEntity();
        testAccount.setAccountNumber(ACCOUNT_NUMBER);
        testAccount.setOwner(testCustomer);
        testAccount.setAccountType("CURRENT");
        testAccount.setBalanceMDL(BigDecimal.valueOf(5000));
        testAccount.setBalanceEUR(BigDecimal.ZERO);
        testAccount.setBalanceUSD(BigDecimal.ZERO);
        testAccount.setBalanceGBP(BigDecimal.ZERO);
        testAccount.setBalanceRON(BigDecimal.ZERO);
        testAccount.setCreationDate(LocalDate.now());
        testAccount.setActive(true);
        testAccount.setDailyWithdrawalLimit(BigDecimal.valueOf(5000));
        testAccount.setDailyWithdrawalUsed(BigDecimal.ZERO);
        testAccount.setLastResetDate(LocalDate.now());

        testAccount.setPasswordHash("Parola1234");

        entityManager.persistAndFlush(testAccount);
        entityManager.clear();
    }

    @Test
    void findByAccountNumber_ShouldReturnAccount() {
        Optional<AccountEntity> found = accountRepository.findByAccountNumber(ACCOUNT_NUMBER);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
    }

    @Test
    void sumAllMDLBalances_ShouldReturnTotal() {
        // Adaugă un al doilea cont
        AccountEntity secondAccount = new AccountEntity();
        secondAccount.setAccountNumber("2345678901234567");
        secondAccount.setOwner(testCustomer);
        secondAccount.setAccountType("SAVINGS");
        secondAccount.setBalanceMDL(BigDecimal.valueOf(3000));
        secondAccount.setCreationDate(LocalDate.now());
        secondAccount.setActive(true);
        secondAccount.setDailyWithdrawalLimit(BigDecimal.valueOf(2000));
        secondAccount.setDailyWithdrawalUsed(BigDecimal.ZERO);
        secondAccount.setLastResetDate(LocalDate.now());

        secondAccount.setPasswordHash("Parola1234");

        entityManager.persistAndFlush(secondAccount);
        entityManager.clear();

        BigDecimal total = accountRepository.sumAllMDLBalances();

        // Folosește isEqualByComparingTo în loc de isEqualTo
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(8000));
    }

    @Test
    void updateAccountStatus_ShouldChangeStatus() {
        int updated = accountRepository.updateAccountStatus(ACCOUNT_NUMBER, false);
        assertThat(updated).isEqualTo(1);

        // Curăță cache-ul înainte de a citi din nou
        entityManager.flush();
        entityManager.clear();

        AccountEntity updatedAccount = entityManager.find(AccountEntity.class, ACCOUNT_NUMBER);
        assertThat(updatedAccount.isActive()).isFalse();
    }

    @Test
    void updateDailyWithdrawalLimit_ShouldChangeLimit() {
        BigDecimal newLimit = BigDecimal.valueOf(10000);

        int updated = accountRepository.updateDailyWithdrawalLimit(ACCOUNT_NUMBER, newLimit);
        assertThat(updated).isEqualTo(1);

        // Curăță cache-ul înainte de a citi din nou
        entityManager.flush();
        entityManager.clear();

        AccountEntity updatedAccount = entityManager.find(AccountEntity.class, ACCOUNT_NUMBER);
        assertThat(updatedAccount.getDailyWithdrawalLimit()).isEqualByComparingTo(newLimit);
    }
}