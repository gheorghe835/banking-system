package com.bank.infrastructure.persistence.mapper;

import com.bank.domain.model.Customer;
import com.bank.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerEntity toEntity(Customer customer) {
        if (customer == null) return null;

        CustomerEntity entity = new CustomerEntity();
        entity.setCustomerId(customer.getCustomerId());
        entity.setFirstName(customer.getFirstName());
        entity.setLastName(customer.getLastName());
        entity.setEmail(customer.getEmail());
        entity.setPhoneNumber(customer.getPhoneNumber());
        entity.setBirthDate(customer.getBirthDate());
        entity.setAddress(customer.getAddress());
        entity.setIdentityNumber(customer.getIdentityNumber());
        entity.setRegistrationDate(customer.getRegistrationDate());
        entity.setActive(customer.isActive());

        return entity;
    }

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;

        Customer customer = new Customer();
        customer.setCustomerId(entity.getCustomerId());
        customer.setFirstName(entity.getFirstName());
        customer.setLastName(entity.getLastName());
        customer.setEmail(entity.getEmail());
        customer.setPhoneNumber(entity.getPhoneNumber());
        customer.setBirthDate(entity.getBirthDate());
        customer.setAddress(entity.getAddress());
        customer.setIdentityNumber(entity.getIdentityNumber());
        customer.setRegistrationDate(entity.getRegistrationDate());
        customer.setActive(entity.isActive());

        return customer;
    }
}