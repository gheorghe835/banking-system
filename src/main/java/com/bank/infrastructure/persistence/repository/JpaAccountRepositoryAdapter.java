package com.bank.infrastructure.persistence.repository;
import com.bank.domain.model.Account;
import com.bank.domain.model.Customer;
import com.bank.domain.repository.AccountRepository;
import com.bank.infrastructure.persistence.entity.AccountEntity;
import com.bank.infrastructure.persistence.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final JpaAccountRepository jpaAccountRepository;
    private final AccountMapper accountMapper;

    @Autowired
    public JpaAccountRepositoryAdapter(JpaAccountRepository jpaAccountRepository,
                                       AccountMapper accountMapper) {
        this.jpaAccountRepository = jpaAccountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity savedEntity = jpaAccountRepository.save(entity);
        return accountMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaAccountRepository.findById(accountNumber)
                .map(accountMapper::toDomain);
    }

    @Override
    public boolean deleteByAccountNumber(String accountNumber) {
        if (jpaAccountRepository.existsById(accountNumber)) {
            jpaAccountRepository.deleteById(accountNumber);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaAccountRepository.existsById(accountNumber);
    }

    @Override
    public List<Account> findAll() {
        return jpaAccountRepository.findAll().stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaAccountRepository.count();
    }

    @Override
    public List<Account> findByCustomer(Customer customer) {
        // Implementează după ce ai CustomerEntity și CustomerRepository
        return List.of();
    }

    @Override
    public List<Account> findByAccountType(String accountType) {
        return jpaAccountRepository.findByAccountType(accountType).stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findActiveAccounts() {
        return jpaAccountRepository.findByActiveTrue().stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findInactiveAccounts() {
        return jpaAccountRepository.findByActiveFalse().stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findByBalanceGreaterThanEqual(BigDecimal minBalance) {
        return jpaAccountRepository.findByBalanceMDLGreaterThanEqual(minBalance).stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findByCreationDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaAccountRepository.findByCreationDateBetween(startDate, endDate).stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findByOwnerNameContaining(String ownerNamePart) {
        return jpaAccountRepository.findByOwnerNameContaining(ownerNamePart).stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalBalanceInMDL() {
        BigDecimal sum = jpaAccountRepository.sumAllMDLBalances();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAverageBalanceInMDL() {
        BigDecimal avg = jpaAccountRepository.averageMDLBalance();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    @Override
    public boolean blockAccount(String accountNumber) {
        return jpaAccountRepository.updateAccountStatus(accountNumber, false) > 0;
    }

    @Override
    public boolean unblockAccount(String accountNumber) {
        return jpaAccountRepository.updateAccountStatus(accountNumber, true) > 0;
    }

    @Override
    public boolean updateDailyWithdrawalLimit(String accountNumber, double newLimit) {
        return jpaAccountRepository.updateDailyWithdrawalLimit(
                accountNumber, BigDecimal.valueOf(newLimit)) > 0;
    }

    @Override
    public int resetDailyWithdrawalUsed() {
        return jpaAccountRepository.resetDailyWithdrawalLimits();
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        return jpaAccountRepository.findByOwnerCustomerId(customerId).stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }
}
