package com.bank.domain.service;

import com.bank.domain.exception.CustomerNotFoundException;
import com.bank.domain.exception.DuplicateCustomerException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Customer;
import com.bank.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ValidationService validationService;

    public CustomerService(CustomerRepository customerRepository,
                           ValidationService validationService) {
        this.customerRepository = customerRepository;
        this.validationService = validationService;
    }

    // ============ CREATE ============

    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public Customer createCustomer(String firstName, String lastName,
                                   String email, String phoneNumber,
                                   LocalDate birthDate, String identityNumber) {

        // Validare
        validationService.validateCustomerName(firstName, lastName);
        validationService.validateEmail(email);
        validationService.validatePhoneNumber(phoneNumber);
        validationService.validateBirthDate(birthDate);
        validationService.validateIdentityNumber(identityNumber);

        // Verifică duplicate
        if (customerRepository.existsByEmail(email)) {
            throw new RuntimeException("Email deja înregistrat: " + email);
        }
        if (customerRepository.existsByIdentityNumber(identityNumber)) {
            throw new RuntimeException("IDNP deja înregistrat: " + identityNumber);
        }

        // Creează și salvează
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setBirthDate(birthDate);
        customer.setIdentityNumber(identityNumber);
        customer.setCustomerId("CUST" + System.currentTimeMillis()); // generează ID
        customer.setRegistrationDate(LocalDateTime.now());
        customer.setActive(true);

        return customerRepository.save(customer);
    }




    // ============ READ ============

    public Customer findCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Cacheable(value = "customers", key = "'email-' + #email", unless = "#result == null")
    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Email: " + email));
    }

    @Cacheable(value = "customers", key = "'idnp-' + #identityNumber", unless = "#result == null")
    public Customer findCustomerByIdentityNumber(String identityNumber) {
        return customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new CustomerNotFoundException("IDNP: " + identityNumber));
    }

    @Cacheable(value = "customers", key = "'all'", unless = "#result == null")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Cacheable(value = "customers", key = "'search-' + #searchTerm", unless = "#result == null")
    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    @Cacheable(value = "customers", key = "'active'", unless = "#result == null")
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }

    public List<Customer> getInactiveCustomers() {
        return customerRepository.findByActiveFalse();
    }

    // ============ UPDATE ============


    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public Customer updateCustomer(String customerId, String firstName,
                                   String lastName, String email,
                                   String phoneNumber, String address) {
        Customer customer = findCustomerById(customerId);

        if (firstName != null && !firstName.isEmpty()) {
            validationService.validateCustomerName(firstName, "prenume");
            customer.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            validationService.validateCustomerName(lastName, "nume");
            customer.setLastName(lastName);
        }
        if (email != null && !email.isEmpty()) {
            validationService.validateEmail(email);
            // Verifică dacă email-ul e deja folosit de alt client
            if (!email.equals(customer.getEmail()) &&
                    customerRepository.existsByEmail(email)) {
                throw new DuplicateCustomerException("Email deja înregistrat: " + email);
            }
            customer.setEmail(email);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            validationService.validatePhoneNumber(phoneNumber);
            customer.setPhoneNumber(phoneNumber);
        }
        if (address != null) {
            customer.setAddress(address);
        }

        return customerRepository.save(customer);
    }


    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public Customer activateCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);
        customer.activate();
        Customer saved = customerRepository.save(customer);
        System.out.println("🔓 Client " + customerId + " activat în baza de date");
        return saved;
    }


    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public Customer deactivateCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);
        customer.deactivate();
        Customer saved = customerRepository.save(customer);
        System.out.println("🔒 Client " + customerId + " dezactivat în baza de date");
        return saved;
    }

    // ============ DELETE ============

    @CacheEvict(value = "customers", allEntries = true)
    @Transactional
    public boolean deleteCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);

        // Verifică dacă mai are conturi active
        if (customer.hasActiveAccounts()) {
            throw new ValidationException(
                    "Nu se poate șterge clientul. Are conturi active.");
        }

        customerRepository.deleteById(customerId);
        return true;
    }

    // ============ UTILITARE ============

    @Cacheable(value = "customers", key = "'count-total'", unless = "#result == null")
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

    @Cacheable(value = "customers", key = "'count-active'", unless = "#result == null")
    public long getActiveCustomerCount() {
        return customerRepository.countByActiveTrue();
    }

    public List<Customer> getCustomersBornAfter(LocalDate date) {
        return customerRepository.findByBirthDateAfter(date);
    }

    public List<Customer> getCustomersBornBefore(LocalDate date) {
        return customerRepository.findByBirthDateBefore(date);
    }
}
