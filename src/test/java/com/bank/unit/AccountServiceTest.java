package com.bank.unit;

import com.bank.domain.exception.*;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ValidationService validationService;

    private AccountService accountService;
    private Account testAccount;
    private Customer testCustomer;

    private final String ACCOUNT_NUMBER = "1234567890123456";

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionRepository, validationService);

        testCustomer = new Customer();
        testCustomer.setCustomerId("CUST001");
        testCustomer.setFirstName("Ion");
        testCustomer.setLastName("Popescu");

        testAccount = new Account(ACCOUNT_NUMBER, testCustomer, Account.ACCOUNT_TYPE_CURRENT);
        testAccount.deposit(BigDecimal.valueOf(1000), Currency.MDL);
    }

    @Test
    void withdraw_InsufficientFunds_ShouldThrowInsufficientFundsException() {
        BigDecimal withdrawAmount = BigDecimal.valueOf(2000); // Mai mult decât soldul

        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testAccount));
        doNothing().when(validationService).validateWithdrawalAmount(any(), any());

        assertThatThrownBy(() ->
                accountService.withdraw(ACCOUNT_NUMBER, withdrawAmount, Currency.MDL)
        ).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void deposit_NegativeAmount_ShouldThrowValidationException() {
        BigDecimal negativeAmount = BigDecimal.valueOf(-100);

        doThrow(new ValidationException("Sumă invalidă"))
                .when(validationService).validateDepositAmount(negativeAmount, Currency.MDL);

        assertThatThrownBy(() ->
                accountService.deposit(ACCOUNT_NUMBER, negativeAmount, Currency.MDL)
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    void blockAccount_ActiveAccount_ShouldDeactivate() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account blocked = accountService.blockAccount(ACCOUNT_NUMBER);

        assertThat(blocked.isActive()).isFalse();
        verify(transactionRepository).save(any());
    }

    @Test
    void unblockAccount_InactiveAccount_ShouldActivate() {
        testAccount.deactivate(); // Mai întâi dezactivează

        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account unblocked = accountService.unblockAccount(ACCOUNT_NUMBER);

        assertThat(unblocked.isActive()).isTrue();
        verify(transactionRepository).save(any());
    }
}