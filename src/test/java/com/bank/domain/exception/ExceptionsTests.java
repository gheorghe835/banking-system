/*package com.bank.domain.exception;

import com.bank.domain.model.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/
  Test pentru excepțiile de domeniu

@DisplayName("Domain Exception Tests")
class ExceptionTests {

    @Test
    @DisplayName("Test BankingErrorCode enum")
    void testBankingErrorCode() {
        assertEquals("ACC-0001", BankingErrorCode.ACCOUNT_NOT_FOUND.getCode());
        assertEquals("Contul nu există", BankingErrorCode.ACCOUNT_NOT_FOUND.getMessage());

        assertEquals(BankingErrorCode.ACCOUNT_NOT_FOUND,
                BankingErrorCode.fromCode("ACC-0001"));

        assertThrows(IllegalArgumentException.class,
                () -> BankingErrorCode.fromCode("INVALID-CODE"));
    }

    @Test
    @DisplayName("Test BankingException")
    void testBankingException() {
        BankingException ex = new BankingException(BankingErrorCode.ACCOUNT_NOT_FOUND,
                "Contul 123 nu există");

        assertEquals(BankingErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
        assertEquals("Contul 123 nu există", ex.getDetails());
        assertTrue(ex.getFullMessage().contains("ACC-0001"));
        assertTrue(ex.getFullMessage().contains("Contul nu există"));
        assertTrue(ex.isAccountNotFound());
    }

    @Test
    @DisplayName("Test AccountNotFoundException")
    void testAccountNotFoundException() {
        AccountNotFoundException ex = new AccountNotFoundException("1234567890123456");

        assertEquals("1234567890123456", ex.getAccountNumber());
        assertEquals(BankingErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("1234567890123456"));
    }

    @Test
    @DisplayName("Test InsufficientFundsException")
    void testInsufficientFundsException() {
        InsufficientFundsException ex = new InsufficientFundsException(
                "1234567890123456", 1000.0, 500.0, Currency.MDL);

        assertEquals("1234567890123456", ex.getAccountNumber());
        assertEquals(1000.0, ex.getRequestedAmount(), 0.001);
        assertEquals(500.0, ex.getAvailableAmount(), 0.001);
        assertEquals(Currency.MDL, ex.getCurrency());
        assertEquals(500.0, ex.getShortfall(), 0.001);
        assertTrue(ex.isInsufficientFunds());
    }

    @Test
    @DisplayName("Test ValidationException")
    void testValidationException() {
        ValidationException ex = new ValidationException("Eroare validare");
        ex.addError("accountNumber", "Număr cont invalid", "123");
        ex.addError("balance", "Sold negativ", -100.0);

        assertEquals(2, ex.getErrorCount());
        assertTrue(ex.hasErrors());

        ValidationException.ValidationError firstError = ex.getErrors().get(0);
        assertEquals("accountNumber", firstError.getField());
        assertEquals("Număr cont invalid", firstError.getMessage());
        assertEquals("123", firstError.getInvalidValue());

        assertTrue(ex.getDetailedMessage().contains("Număr cont invalid"));
    }

    @Test
    @DisplayName("Test SecurityException")
    void testSecurityException() {
        BankingSecurityException ex = new BankingSecurityException(
                BankingErrorCode.INVALID_CREDENTIALS,
                "admin",
                BankingSecurityException.SecurityAction.LOGIN_ATTEMPT,
                "192.168.1.1"
        );

        assertEquals("admin", ex.getUsername());
        assertEquals(BankingSecurityException.SecurityAction.LOGIN_ATTEMPT, ex.getAction());
        assertEquals("192.168.1.1", ex.getIpAddress());
        assertTrue(ex.isLoginRelated());
        assertTrue(ex.isSecurityError());
    }

    @Test
    @DisplayName("Test exception inheritance")
    void testExceptionInheritance() {
        // Verifică că toate excepțiile extind BankingException
        assertTrue(new AccountNotFoundException("123") instanceof BankingException);
        assertTrue(new InsufficientFundsException("123", 100, 50, Currency.MDL) instanceof BankingException);
        assertTrue(new BankingSecurityException(BankingErrorCode.UNAUTHORIZED_ACCESS, "user",
                BankingSecurityException.SecurityAction.ACCOUNT_ACCESS)
                instanceof BankingException);
    }
}
*/