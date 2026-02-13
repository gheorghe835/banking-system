package com.bank.domain.service;

import com.bank.domain.exception.CustomerNotFoundException;
import com.bank.domain.exception.DuplicateCustomerException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Customer;
import com.bank.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            throw new DuplicateCustomerException("Email deja înregistrat: " + email);
        }
        if (customerRepository.existsByIdentityNumber(identityNumber)) {
            throw new DuplicateCustomerException("IDNP deja înregistrat: " + identityNumber);
        }

        // Creează și salvează
        Customer customer = new Customer(firstName, lastName, email,
                phoneNumber, birthDate, identityNumber);
        return customerRepository.save(customer);
    }

    // ============ READ ============

    public Customer findCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Email: " + email));
    }

    public Customer findCustomerByIdentityNumber(String identityNumber) {
        return customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new CustomerNotFoundException("IDNP: " + identityNumber));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    public List<Customer> getActiveCustomers() {
        return customerRepository.findByActiveTrue();
    }

    public List<Customer> getInactiveCustomers() {
        return customerRepository.findByActiveFalse();
    }

    // ============ UPDATE ============

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

    public Customer activateCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);
        customer.activate();
        return customerRepository.save(customer);
    }

    public Customer deactivateCustomer(String customerId) {
        Customer customer = findCustomerById(customerId);
        customer.deactivate();
        return customerRepository.save(customer);
    }

    // ============ DELETE ============

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

    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

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
