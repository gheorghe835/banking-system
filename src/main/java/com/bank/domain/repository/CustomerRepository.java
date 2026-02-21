package com.bank.domain.repository;

import com.bank.domain.model.Customer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    // CRUD
    Customer save(Customer customer);
    Optional<Customer> findById(String customerId);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdentityNumber(String identityNumber);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    List<Customer> findAll();
    void deleteById(String customerId);
    boolean existsById(String customerId);
    boolean existsByEmail(String email);
    boolean existsByIdentityNumber(String identityNumber);
    long count();

    // Business methods
    List<Customer> findByActiveTrue();
    List<Customer> findByActiveFalse();
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    List<Customer> findByBirthDateAfter(LocalDate date);
    List<Customer> findByBirthDateBefore(LocalDate date);
    List<Customer> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
    long countByActiveTrue();
    long countByActiveFalse();

    
}
