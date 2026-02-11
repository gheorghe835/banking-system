package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.BankManagerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaBankManagerRepository extends JpaRepository<BankManagerEntity, String> {

    // Basic queries
    Optional<BankManagerEntity> findByUsername(String username);
    Optional<BankManagerEntity> findByEmail(String email);

    // Find by access level
    List<BankManagerEntity> findByAccessLevel(String accessLevel);

    // Find by department
    List<BankManagerEntity> findByDepartment(String department);

    // Find active/inactive managers
    List<BankManagerEntity> findByActiveTrue();
    List<BankManagerEntity> findByActiveFalse();

    // Find by name
    List<BankManagerEntity> findByFirstNameContainingIgnoreCase(String firstName);
    List<BankManagerEntity> findByLastNameContainingIgnoreCase(String lastName);
    List<BankManagerEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    // Check existence
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Find managers with admin access
    List<BankManagerEntity> findByAccessLevelIn(List<String> adminLevels);
}
