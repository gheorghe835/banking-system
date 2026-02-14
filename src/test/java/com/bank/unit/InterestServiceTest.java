package com.bank.unit;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.InterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    private InterestService interestService;

    private Account savingsAccount;
    private Account currentAccount;
    private final String SAVINGS_ACCOUNT = "1234567890123456";
    private final String CURRENT_ACCOUNT = "6543210987654321";

    @BeforeEach
    void setUp() {
        interestService = new InterestService(accountRepository, transactionRepository, accountService);

        Customer customer = new Customer();
        customer.setFirstName("Ion");
        customer.setLastName("Popescu");

        savingsAccount = new Account(SAVINGS_ACCOUNT, customer, Account.ACCOUNT_TYPE_SAVINGS);
        savingsAccount.deposit(BigDecimal.valueOf(10000), Currency.MDL);

        currentAccount = new Account(CURRENT_ACCOUNT, customer, Account.ACCOUNT_TYPE_CURRENT);
        currentAccount.deposit(BigDecimal.valueOf(5000), Currency.MDL);
    }

    @Test
    void calculateInterest_SavingsAccount_ShouldReturnCorrectAmount() {
        // Sold 10000 MDL, rata 3.5%, 30 zile
        // Dobânda = 10000 * (3.5/36500) * 30 = 28.77
        BigDecimal interest = interestService.calculateInterestForAccount(savingsAccount, 30);

        assertThat(interest).isEqualByComparingTo(BigDecimal.valueOf(28.77).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void calculateInterest_CurrentAccount_ShouldReturnCorrectAmount() {
        // Sold 5000 MDL, rata 0.5%, 30 zile
        // Dobânda = 5000 * (0.5/36500) * 30 = 2.05
        BigDecimal interest = interestService.calculateInterestForAccount(currentAccount, 30);

        assertThat(interest).isEqualByComparingTo(BigDecimal.valueOf(2.05).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void calculateInterest_BelowMinimumBalance_ShouldReturnZero() {
        Account smallAccount = new Account("1111111111111111", new Customer(), Account.ACCOUNT_TYPE_SAVINGS);
        smallAccount.deposit(BigDecimal.valueOf(50), Currency.MDL);

        BigDecimal interest = interestService.calculateInterestForAccount(smallAccount, 30);

        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateInterest_InactiveAccount_ShouldReturnZero() {
        savingsAccount.deactivate();

        BigDecimal interest = interestService.calculateInterestForAccount(savingsAccount, 30);

        assertThat(interest).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void isEarningInterest_AboveMinimum_ShouldReturnTrue() {
        boolean earning = interestService.isEarningInterest(savingsAccount);
        assertThat(earning).isTrue();
    }

    @Test
    void isEarningInterest_BelowMinimum_ShouldReturnFalse() {
        Account smallAccount = new Account("1111111111111111", new Customer(), Account.ACCOUNT_TYPE_SAVINGS);
        smallAccount.deposit(BigDecimal.valueOf(50), Currency.MDL);

        boolean earning = interestService.isEarningInterest(smallAccount);
        assertThat(earning).isFalse();
    }

    @Test
    void calculateTotalInterestProjection_ShouldReturnSum() {
        List<Account> accounts = List.of(savingsAccount, currentAccount);
        when(accountRepository.findActiveAccounts()).thenReturn(accounts);

        BigDecimal total = interestService.calculateTotalInterestProjection(30);

        // 28.77 + 2.05 = 30.82
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(30.82).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void calculateTimeToTarget_ShouldReturnDaysNeeded() {
        BigDecimal target = BigDecimal.valueOf(11000);

        int days = interestService.calculateTimeToTarget(savingsAccount, target, null);

        // Dobândă zilnică = 10000 * (3.5/36500) = 0.96
        // Necesar 1000 MDL, deci ~1042 zile
        assertThat(days).isPositive();
    }
}
