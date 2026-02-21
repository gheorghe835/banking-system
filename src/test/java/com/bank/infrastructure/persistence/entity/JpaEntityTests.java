/*package com.bank.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Entity Tests")
class JpaEntityTests {

    @Test
    @DisplayName("Test creare CustomerEntity")
    void testCustomerEntityCreation() {
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId("CUST001");
        customer.setFirstName("Egor");
        customer.setLastName("Batiri");
        customer.setEmail("egor@example.com");
        customer.setPhoneNumber("+37369123456");
        customer.setBirthDate(LocalDate.of(2000, 12, 17));
        customer.setIdentityNumber("1234567890123");
        customer.setActive(true);

        assertEquals("CUST001", customer.getCustomerId());
        assertEquals("Egor Batiri", customer.getFullName());
        assertEquals("egor@example.com", customer.getEmail());
        assertTrue(customer.isActive());
        assertNotNull(customer.getRegistrationDate());
    }

    @Test
    @DisplayName("Test creare AccountEntity")
    void testAccountEntityCreation() {
        // Creează customer
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId("CUST001");
        customer.setFirstName("Iulia");
        customer.setLastName("Batiri");

        // Creează cont
        AccountEntity account = new AccountEntity();
        account.setAccountNumber("1234567890123456");
        account.setOwner(customer);
        account.setAccountType("CURRENT");
        account.setBalanceMDL(BigDecimal.valueOf(1000));
        account.setActive(true);

        assertEquals("1234567890123456", account.getAccountNumber());
        assertEquals(customer, account.getOwner());
        assertEquals("CURRENT", account.getAccountType());
        assertEquals(BigDecimal.valueOf(1000), account.getBalanceMDL());
        assertTrue(account.isActive());
        assertNotNull(account.getCreationDate());
    }

    @Test
    @DisplayName("Test creare TransactionEntity")
    void testTransactionEntityCreation() {
        // Creează account
        AccountEntity account = new AccountEntity();
        account.setAccountNumber("1234567890123456");

        // Creează tranzacție
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionId("TRX001");
        transaction.setAccount(account);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(BigDecimal.valueOf(500));
        transaction.setCurrency("EUR");
        transaction.setDescription("Test deposit");
        transaction.markAsCompleted();

        assertEquals("TRX001", transaction.getTransactionId());
        assertEquals(account, transaction.getAccount());
        assertEquals("DEPOSIT", transaction.getTransactionType());
        assertEquals(BigDecimal.valueOf(500), transaction.getAmount());
        assertEquals("EUR", transaction.getCurrency());
        assertTrue(transaction.isCompleted());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    @DisplayName("Test creare BankManagerEntity")
    void testBankManagerEntityCreation() {
        BankManagerEntity manager = new BankManagerEntity();
        manager.setEmployeeId("MGR001");
        manager.setUsername("admin");
        manager.setPasswordHash("hashed_password");
        manager.setFirstName("Oxana");
        manager.setLastName("Batiri");
        manager.setEmail("oxana@bank.com");
        manager.setAccessLevel("ADMIN");
        manager.setActive(true);

        assertEquals("MGR001", manager.getEmployeeId());
        assertEquals("admin", manager.getUsername());
        assertEquals("Oxana Batiri", manager.getFullName());
        assertEquals("ADMIN", manager.getAccessLevel());
        assertTrue(manager.hasAdminAccess());
        assertTrue(manager.isActive());
    }

    @Test
    @DisplayName("Test relationships between entities")
    void testEntityRelationships() {
        // Creează customer
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerId("CUST001");
        customer.setFirstName("Test");
        customer.setLastName("Customer");

        // Creează account
        AccountEntity account = new AccountEntity();
        account.setAccountNumber("1234567890123456");
        account.setOwner(customer);

        // Creează transaction
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionId("TRX001");
        transaction.setAccount(account);
        transaction.setTransactionType("DEPOSIT");

        // Verifică relațiile
        assertEquals(customer, account.getOwner());
        assertEquals(account, transaction.getAccount());
    }
}
*/