// Infrastructure Layer - Repository pentru JPA
package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaAccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Transactional
    @Modifying
    @Query("DELETE FROM AccountEntity a WHERE a.accountNumber = :accountNumber")
    int deleteByAccountNumber(@Param("accountNumber") String accountNumber);

    List<AccountEntity> findByOwner_CustomerId(String customerId);

    List<AccountEntity> findByAccountType(String accountType);

    List<AccountEntity> findByActiveTrue();

    List<AccountEntity> findByActiveFalse();

    // Această metodă necesită un field totalBalanceInMDL în AccountEntity
    @Query("SELECT a FROM AccountEntity a WHERE " +
            "(a.balanceMDL + a.balanceEUR * 19.45 + a.balanceUSD * 17.55 + " +
            "a.balanceGBP * 22.10 + a.balanceRON * 4.0) >= :minBalance")
    List<AccountEntity> findByTotalBalanceGreaterThanEqual(@Param("minBalance") double minBalance);

    List<AccountEntity> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM AccountEntity a WHERE " +
            "LOWER(CONCAT(a.owner.firstName, ' ', a.owner.lastName)) " +
            "LIKE LOWER(CONCAT('%', :ownerNamePart, '%'))")
    List<AccountEntity> findByOwnerNameContaining(@Param("ownerNamePart") String ownerNamePart);

    @Query("SELECT COALESCE(" +
            "SUM(a.balanceMDL + a.balanceEUR * 19.45 + a.balanceUSD * 17.55 + " +
            "a.balanceGBP * 22.10 + a.balanceRON * 4.0), 0) FROM AccountEntity a")
    double getTotalBalanceInMDL();

    @Query("SELECT COALESCE(" +
            "AVG(a.balanceMDL + a.balanceEUR * 19.45 + a.balanceUSD * 17.55 + " +
            "a.balanceGBP * 22.10 + a.balanceRON * 4.0), 0) FROM AccountEntity a")
    double getAverageBalanceInMDL();

    @Query("SELECT a FROM AccountEntity a ORDER BY " +
            "(a.balanceMDL + a.balanceEUR * 19.45 + a.balanceUSD * 17.55 + " +
            "a.balanceGBP * 22.10 + a.balanceRON * 4.0) DESC")
    Optional<AccountEntity> findTopByOrderByTotalBalanceDesc();

    @Query("SELECT a FROM AccountEntity a ORDER BY " +
            "(a.balanceMDL + a.balanceEUR * 19.45 + a.balanceUSD * 17.55 + " +
            "a.balanceGBP * 22.10 + a.balanceRON * 4.0) ASC")
    Optional<AccountEntity> findTopByOrderByTotalBalanceAsc();

    @Transactional
    @Modifying
    @Query("UPDATE AccountEntity a SET a.active = false WHERE a.accountNumber = :accountNumber")
    int deactivateByAccountNumber(@Param("accountNumber") String accountNumber);

    @Transactional
    @Modifying
    @Query("UPDATE AccountEntity a SET a.active = true WHERE a.accountNumber = :accountNumber")
    int activateByAccountNumber(@Param("accountNumber") String accountNumber);

    @Transactional
    @Modifying
    @Query("UPDATE AccountEntity a SET a.dailyWithdrawalLimit = :newLimit " +
            "WHERE a.accountNumber = :accountNumber")
    int updateDailyLimit(@Param("accountNumber") String accountNumber,
                         @Param("newLimit") double newLimit);

    @Transactional
    @Modifying
    @Query("UPDATE AccountEntity a SET a.dailyWithdrawalUsed = 0, a.lastResetDate = CURRENT_DATE")
    int resetDailyWithdrawals();
}


