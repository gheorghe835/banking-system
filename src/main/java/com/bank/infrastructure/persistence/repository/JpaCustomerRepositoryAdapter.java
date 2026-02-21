package com.bank.infrastructure.persistence.repository;

import com.bank.domain.model.Customer;
import com.bank.domain.repository.CustomerRepository;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import com.bank.infrastructure.persistence.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaCustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository jpaCustomerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public JpaCustomerRepositoryAdapter(JpaCustomerRepository jpaCustomerRepository,
                                        CustomerMapper customerMapper) {
        this.jpaCustomerRepository = jpaCustomerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customerMapper.toEntity(customer);
        if (entity.getCustomerId() == null) {
            entity.setCustomerId("CUST" + System.currentTimeMillis() % 10000);
        }
        CustomerEntity savedEntity = jpaCustomerRepository.save(entity);
        return customerMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        return jpaCustomerRepository.findById(customerId)
                .map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaCustomerRepository.findByEmail(email)
                .map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIdentityNumber(String identityNumber) {
        return jpaCustomerRepository.findByIdentityNumber(identityNumber)
                .map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        return jpaCustomerRepository.findByPhoneNumber(phoneNumber)
                .map(customerMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return jpaCustomerRepository.findAll().stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String customerId) {
        jpaCustomerRepository.deleteById(customerId);
    }

    @Override
    public boolean existsById(String customerId) {
        return jpaCustomerRepository.existsById(customerId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaCustomerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdentityNumber(String identityNumber) {
        return jpaCustomerRepository.existsByIdentityNumber(identityNumber);
    }

    @Override
    public long count() {
        return jpaCustomerRepository.count();
    }

    @Override
    public List<Customer> findByActiveTrue() {
        return jpaCustomerRepository.findByActiveTrue().stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByActiveFalse() {
        return jpaCustomerRepository.findByActiveFalse().stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByLastNameContainingIgnoreCase(String lastName) {
        return jpaCustomerRepository.findByLastNameContainingIgnoreCase(lastName).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName) {
        return jpaCustomerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(firstName, lastName)
                .stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByBirthDateAfter(LocalDate date) {
        return jpaCustomerRepository.findByBirthDateAfter(date).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByBirthDateBefore(LocalDate date) {
        return jpaCustomerRepository.findByBirthDateBefore(date).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaCustomerRepository.findByBirthDateBetween(startDate, endDate).stream()
                .map(customerMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByActiveTrue() {
        return jpaCustomerRepository.countByActiveTrue();
    }

    @Override
    public long countByActiveFalse() {
        return jpaCustomerRepository.countByActiveFalse();
    }
}
