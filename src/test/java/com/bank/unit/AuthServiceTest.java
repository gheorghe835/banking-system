package com.bank.unit;

import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.BankingSecurityException;
import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Customer;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import com.bank.domain.model.Currency;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountService accountService;

    private AuthService authService;

    private Account testAccount;
    private final String ACCOUNT_NUMBER = "1234567890123456";
    private final String CORRECT_PASSWORD = "Parola1234";
    private final String WRONG_PASSWORD = "gresit";

    @BeforeEach
    void setUp() {
        authService = new AuthService(accountService);

        Customer customer = new Customer();
        customer.setFirstName("Ion");
        customer.setLastName("Popescu");

        testAccount = new Account(ACCOUNT_NUMBER, customer, Account.ACCOUNT_TYPE_CURRENT);
        testAccount.deposit(BigDecimal.valueOf(1000), Currency.MDL);
    }

    @Test
    void authenticateClient_CorrectCredentials_ShouldReturnAccount() {
        when(accountService.findAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        Account authenticated = authService.authenticateClient(ACCOUNT_NUMBER, CORRECT_PASSWORD);

        assertThat(authenticated).isNotNull();
        assertThat(authenticated.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
    }

    @Test
    void authenticateClient_WrongPassword_ShouldThrowException() {
        when(accountService.findAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        assertThatThrownBy(() ->
                authService.authenticateClient(ACCOUNT_NUMBER, WRONG_PASSWORD)
        ).isInstanceOf(BankingSecurityException.class)
                .hasMessageContaining("Credențiale invalide");
    }

    @Test
    void authenticateClient_NonExistingAccount_ShouldThrowException() {
        when(accountService.findAccount(anyString())).thenThrow(new AccountNotFoundException(ACCOUNT_NUMBER));

        assertThatThrownBy(() ->
                authService.authenticateClient(ACCOUNT_NUMBER, CORRECT_PASSWORD)
        ).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void authenticateClient_MaxFailedAttempts_ShouldLockAccount() {
        when(accountService.findAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        // Prima încercare greșită
        assertThatThrownBy(() -> authService.authenticateClient(ACCOUNT_NUMBER, WRONG_PASSWORD))
                .isInstanceOf(BankingSecurityException.class);

        // A doua încercare greșită
        assertThatThrownBy(() -> authService.authenticateClient(ACCOUNT_NUMBER, WRONG_PASSWORD))
                .isInstanceOf(BankingSecurityException.class);

        // A treia încercare greșită - ar trebui să blocheze contul
        assertThatThrownBy(() -> authService.authenticateClient(ACCOUNT_NUMBER, WRONG_PASSWORD))
                .isInstanceOf(BankingSecurityException.class)
                .hasMessageContaining("Prea multe încercări");

        assertThat(authService.isAccountLocked(ACCOUNT_NUMBER)).isTrue();
    }

    @Test
    void authenticateManager_CorrectCredentials_ShouldReturnManager() {
        BankManager manager = authService.authenticateManager("admin", "Admin1234");

        assertThat(manager).isNotNull();
        assertThat(manager.getUsername()).isEqualTo("admin");
        assertThat(manager.getAccessLevel()).isEqualTo(BankManager.AccessLevel.ADMIN);
    }

    @Test
    void authenticateManager_WrongCredentials_ShouldThrowException() {
        assertThatThrownBy(() ->
                authService.authenticateManager("admin", "wrong")
        ).isInstanceOf(BankingSecurityException.class);
    }

    @Test
    void unlockAccount_LockedAccount_ShouldRemoveLock() {
        // Blochează contul
        when(accountService.findAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        for (int i = 0; i < 3; i++) {
            try {
                authService.authenticateClient(ACCOUNT_NUMBER, WRONG_PASSWORD);
            } catch (Exception e) {
                // Ignorăm excepțiile
            }
        }

        assertThat(authService.isAccountLocked(ACCOUNT_NUMBER)).isTrue();

        authService.unlockAccount(ACCOUNT_NUMBER);

        assertThat(authService.isAccountLocked(ACCOUNT_NUMBER)).isFalse();
        assertThat(authService.getFailedAttempts(ACCOUNT_NUMBER)).isZero();
    }
}
