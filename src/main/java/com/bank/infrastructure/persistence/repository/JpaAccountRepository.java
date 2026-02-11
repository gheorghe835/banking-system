package com.bank.infrastructure.persistence.repository;

import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaAccountRepository extends JpaRepository<AccountEntity, String> {

    // Basic queries
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    // Find by customer
    List<AccountEntity> findByOwner(CustomerEntity owner);
    List<AccountEntity> findByOwnerCustomerId(String customerId);

    // Find by account type
    List<AccountEntity> findByAccountType(String accountType);

    // Find active/inactive accounts
    List<AccountEntity> findByActiveTrue();
    List<AccountEntity> findByActiveFalse();

    // Find by balance ranges
    List<AccountEntity> findByBalanceMDLGreaterThanEqual(BigDecimal minBalance);
    List<AccountEntity> findByBalanceMDLLessThanEqual(BigDecimal maxBalance);
    List<AccountEntity> findByBalanceMDLBetween(BigDecimal minBalance, BigDecimal maxBalance);

    // Find by creation date
    List<AccountEntity> findByCreationDateAfter(LocalDate date);
    List<AccountEntity> findByCreationDateBefore(LocalDate date);
    List<AccountEntity> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by last login
    List<AccountEntity> findByLastLoginIsNotNull();
    List<AccountEntity> findByLastLoginIsNull();

    // Find accounts with foreign currency
    List<AccountEntity> findByBalanceEURGreaterThan(BigDecimal zero);
    List<AccountEntity> findByBalanceUSDGreaterThan(BigDecimal zero);
    List<AccountEntity> findByBalanceGBPGreaterThan(BigDecimal zero);
    List<AccountEntity> findByBalanceRONGreaterThan(BigDecimal zero);

    // Custom queries with JPQL
    @Query("SELECT a FROM AccountEntity a WHERE a.owner.firstName LIKE %:name% OR a.owner.lastName LIKE %:name%")
    List<AccountEntity> findByOwnerNameContaining(@Param("name") String name);

    @Query("SELECT a FROM AccountEntity a WHERE " +
            "(a.balanceMDL + a.balanceEUR * :eurRate + a.balanceUSD * :usdRate + " +
            "a.balanceGBP * :gbpRate + a.balanceRON * :ronRate) >= :minTotal")
    List<AccountEntity> findByTotalBalanceGreaterThanEqual(
            @Param("minTotal") BigDecimal minTotal,
            @Param("eurRate") BigDecimal eurRate,
            @Param("usdRate") BigDecimal usdRate,
            @Param("gbpRate") BigDecimal gbpRate,
            @Param("ronRate") BigDecimal ronRate);

    // Statistics queries
    @Query("SELECT COUNT(a) FROM AccountEntity a WHERE a.active = true")
    long countActiveAccounts();

    @Query("SELECT SUM(a.balanceMDL) FROM AccountEntity a")
    BigDecimal sumAllMDLBalances();

    @Query("SELECT AVG(a.balanceMDL) FROM AccountEntity a WHERE a.balanceMDL > 0")
    BigDecimal averageMDLBalance();

    @Query("SELECT MAX(a.balanceMDL) FROM AccountEntity a")
    BigDecimal findMaxMDLBalance();

    @Query("SELECT MIN(a.balanceMDL) FROM AccountEntity a WHERE a.balanceMDL > 0")
    BigDecimal findMinMDLBalance();

    // Update queries
    @Modifying
    @Query("UPDATE AccountEntity a SET a.active = :active WHERE a.accountNumber = :accountNumber")
    int updateAccountStatus(@Param("accountNumber") String accountNumber,
                            @Param("active") boolean active);

    @Modifying
    @Query("UPDATE AccountEntity a SET a.dailyWithdrawalLimit = :newLimit WHERE a.accountNumber = :accountNumber")
    int updateDailyWithdrawalLimit(@Param("accountNumber") String accountNumber,
                                   @Param("newLimit") BigDecimal newLimit);

    @Modifying
    @Query("UPDATE AccountEntity a SET a.dailyWithdrawalUsed = 0, a.lastResetDate = CURRENT_DATE " +
            "WHERE a.lastResetDate < CURRENT_DATE")
    int resetDailyWithdrawalLimits();

    // Native SQL query example
    @Query(value = "SELECT * FROM accounts WHERE " +
            "(balance_mdl + balance_eur * :eurRate + balance_usd * :usdRate + " +
            "balance_gbp * :gbpRate + balance_ron * :ronRate) = " +
            "(SELECT MAX(balance_mdl + balance_eur * :eurRate + balance_usd * :usdRate + " +
            "balance_gbp * :gbpRate + balance_ron * :ronRate) FROM accounts)",
            nativeQuery = true)
    List<AccountEntity> findRichestAccount(@Param("eurRate") BigDecimal eurRate,
                                           @Param("usdRate") BigDecimal usdRate,
                                           @Param("gbpRate") BigDecimal gbpRate,
                                           @Param("ronRate") BigDecimal ronRate);
}
