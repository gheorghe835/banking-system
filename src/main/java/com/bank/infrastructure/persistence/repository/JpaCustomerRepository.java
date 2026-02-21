package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, String> {

    // Găsește prin e-mail
    Optional<CustomerEntity> findByEmail(String email);

    // Găsește după numărul de identificare
    Optional<CustomerEntity> findByIdentityNumber(String identityNumber);

    // Găsește după numărul de telefon
    Optional<CustomerEntity> findByPhoneNumber(String phoneNumber);

// Găsește clienți activi
    List<CustomerEntity> findByActiveTrue();

    // Găsește clienți inactivi
    List<CustomerEntity> findByActiveFalse();

    //  Găsește după nume (fără distincție între majuscule și minuscule)
    List<CustomerEntity> findByLastNameContainingIgnoreCase(String lastName);

    //  Găsește după căutarea numelui complet
    List<CustomerEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Găsește clienți născuți după o anumită dată
    List<CustomerEntity> findByBirthDateAfter(LocalDate date);

    // Găsește clienți născuți înainte de o anumită dată
    List<CustomerEntity> findByBirthDateBefore(LocalDate date);

    // Găsește clienți după intervalul de vârstă
    List<CustomerEntity> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    //  Verifica dacă există o adresă de e-mail
    boolean existsByEmail(String email);

    // Verifica dacă există un număr de identificare
    boolean existsByIdentityNumber(String identityNumber);

    // Numără clienții activi
    long countByActiveTrue();

    // Numără clienții inactivi
    long countByActiveFalse();
}
