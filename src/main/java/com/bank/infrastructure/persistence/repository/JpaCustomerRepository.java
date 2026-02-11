package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, String> {

    // Find by email
    Optional<CustomerEntity> findByEmail(String email);

    // Find by identity number
    Optional<CustomerEntity> findByIdentityNumber(String identityNumber);

    // Find by phone number
    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

    // Find active customers
    List<CustomerEntity> findByActiveTrue();

    // Find inactive customers
    List<CustomerEntity> findByActiveFalse();

    // Find by last name (case insensitive)
    List<CustomerEntity> findByLastNameContainingIgnoreCase(String lastName);

    // Find by full name search
    List<CustomerEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Find customers born after a date
    List<CustomerEntity> findByBirthDateAfter(LocalDate date);

    // Find customers born before a date
    List<CustomerEntity> findByBirthDateBefore(LocalDate date);

    // Find customers by age range
    List<CustomerEntity> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if identity number exists
    boolean existsByIdentityNumber(String identityNumber);

    // Count active customers
    long countByActiveTrue();

    // Count inactive customers
    long countByActiveFalse();
}
