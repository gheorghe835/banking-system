package com.bank.domain.exception;

import com.bank.domain.model.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Exception Tests")
class ExceptionTests {

    @Test
    @DisplayName("Test BankingErrorCode")
    void testBankingErrorCode() {
        assertEquals("ACC-0001", BankingErrorCode.ACCOUNT_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("Test BankingException")
    void testBankingException() {
        BankingException ex = new BankingException(BankingErrorCode.ACCOUNT_NOT_FOUND);
        assertEquals(BankingErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Test AccountNotFoundException")
    void testAccountNotFoundException() {
        AccountNotFoundException ex = new AccountNotFoundException("123");
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    @DisplayName("Test InsufficientFundsException")
    void testInsufficientFundsException() {
        InsufficientFundsException ex = new InsufficientFundsException(
                "123", 1000.0, 500.0, Currency.MDL);
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    @DisplayName("Test ValidationException")
    void testValidationException() {

        ValidationException ex = new ValidationException("Test error");
        ex.addError("field1", "error1", "value1");

        assertTrue(ex.hasErrors());
        assertEquals(1, ex.getErrorCount());

        // Test factory method
        ValidationException ex2 = ValidationException.withError("email", "Invalid", "test@");
        assertEquals(1, ex2.getErrorCount());
    }

    @Test
    @DisplayName("Test SecurityException - simplified")
    void testSecurityException() {
        SecurityException ex = new SecurityException(
                BankingErrorCode.INVALID_CREDENTIALS,
                "admin",
                SecurityException.SecurityAction.LOGIN_ATTEMPT
        );

        assertEquals("admin", ex.getUsername());
        assertEquals(SecurityException.SecurityAction.LOGIN_ATTEMPT, ex.getAction());
    }

    @Test
    @DisplayName("Test CurrencyExchangeException")
    void testCurrencyExchangeException() {
        CurrencyExchangeException ex = new CurrencyExchangeException(
                Currency.EUR, Currency.USD, 100.0, "Test error"
        );
        assertTrue(ex.getMessage().contains("EUR"));
    }

    @Test
    @DisplayName("Test inheritance")
    void testInheritance() {
        assertTrue(new AccountNotFoundException("123") instanceof BankingException);
    }
}