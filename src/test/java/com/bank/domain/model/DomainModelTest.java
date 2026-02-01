package com.bank.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    @DisplayName("Test creare Currency")
    void testCurrencyEnum() {
        // Test valute
        assertEquals("EUR", Currency.EUR.getCode());
        assertEquals(19.45, Currency.EUR.getExchangeRateToMDL(), 0.001);

        // Test conversie
        double converted = Currency.EUR.convertTo(Currency.MDL, 100);
        assertEquals(1945.0, converted, 0.001);

        // Test validare
        assertTrue(Currency.isValid("USD"));
        assertFalse(Currency.isValid("XYZ"));
    }

    @Test
    @DisplayName("Test creare Customer")
    void testCustomerCreation() {
        Customer customer = new Customer(
                "John",
                "Doe",
                "john.doe@example.com",
                "+37369123456",
                LocalDate.of(1990, 1, 1),
                "1234567890123"
        );

        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertTrue(customer.isActive());
        assertNotNull(customer.getCustomerId());
    }

    @Test
    @DisplayName("Test creare Account")
    void testAccountCreation() {
        Customer customer = new Customer(
                "Jane", "Smith", "jane@example.com",
                "+37369234567", LocalDate.of(1985, 5, 15), "9876543210987"
        );

        Account account = new Account(
                "1234567890123456",
                customer,
                Account.ACCOUNT_TYPE_CURRENT,
                BigDecimal.valueOf(1000)
        );

        assertEquals("1234567890123456", account.getAccountNumber());
        assertEquals(customer, account.getOwner());
        assertEquals(Account.ACCOUNT_TYPE_CURRENT, account.getAccountType());
        assertTrue(account.isActive());
        assertEquals(BigDecimal.valueOf(1000), account.getBalance(Currency.MDL));
    }

    @Test
    @DisplayName("Test creare Transaction")
    void testTransactionCreation() {
        Transaction transaction = new Transaction(
                Transaction.TransactionType.DEPOSIT,
                BigDecimal.valueOf(500),
                Currency.EUR,
                "Test deposit"
        );

        assertNotNull(transaction.getTransactionId());
        assertEquals(Transaction.TransactionType.DEPOSIT, transaction.getType());
        assertEquals(BigDecimal.valueOf(500), transaction.getAmount());
        assertEquals(Currency.EUR, transaction.getCurrency());
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    @DisplayName("Test creare BankManager")
    void testBankManagerCreation() {
        BankManager manager = new BankManager(
                "admin123",
                "Alice",
                "Johnson",
                "alice.johnson@bank.com",
                BankManager.AccessLevel.ADMIN
        );

        assertEquals("admin123", manager.getUsername());
        assertEquals("Alice Johnson", manager.getFullName());
        assertEquals(BankManager.AccessLevel.ADMIN, manager.getAccessLevel());
        assertTrue(manager.isActive());
        assertTrue(manager.hasAdminAccess());
    }
}
