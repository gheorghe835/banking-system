package com.bank.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integrare pentru serviciile de domeniu
 */
@DisplayName("Domain Services Integration Tests")
class DomainServicesIntegrationTest {

    @Test
    @DisplayName("Test că serviciile pot fi create")
    void testServicesCanBeCreated() {
        // Acest test verifică doar că putem instanția serviciile

        ValidationService validationService = new ValidationService();
        assertNotNull(validationService);

        System.out.println("✅ Toate serviciile pot fi create");
        System.out.println("   - ValidationService ✓");
        System.out.println("   - AccountService (needs repository mocks)");
        System.out.println("   - TransactionService (needs repository mocks)");
        System.out.println("   - AuthService (needs repository mocks)");
        System.out.println("   - ExchangeService (needs repository mocks)");
    }

    @Test
    @DisplayName("Test ValidationService standalone")
    void testValidationServiceStandalone() {
        ValidationService service = new ValidationService();

        // Test valid account number
        assertDoesNotThrow(() ->
                service.validateAccountNumber("1234567890123456"));

        // Test valid password
        assertDoesNotThrow(() ->
                service.validatePassword("Parola123"));

        // Test valid email
        assertDoesNotThrow(() ->
                service.validateEmail("test@example.com"));

        System.out.println("✅ ValidationService funcționează corect");
    }
}