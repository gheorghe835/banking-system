package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.BankManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBankManagerRepository extends JpaRepository<BankManagerEntity, String> {

    // Interogări de bază
    Optional<BankManagerEntity> findByUsername(String username);
    Optional<BankManagerEntity> findByEmail(String email);

    //  Găsește după nivelul de acces
    List<BankManagerEntity> findByAccessLevel(String accessLevel);

    // Găsește după departament
    List<BankManagerEntity> findByDepartment(String department);

    //  Găsește manageri activi/inactivi
    List<BankManagerEntity> findByActiveTrue();
    List<BankManagerEntity> findByActiveFalse();

    //  Găsește după nume
    List<BankManagerEntity> findByFirstNameContainingIgnoreCase(String firstName);
    List<BankManagerEntity> findByLastNameContainingIgnoreCase(String lastName);
    List<BankManagerEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Verifica existenta
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    //  Găsește administratori cu acces de administrator
    List<BankManagerEntity> findByAccessLevelIn(List<String> adminLevels);
}
