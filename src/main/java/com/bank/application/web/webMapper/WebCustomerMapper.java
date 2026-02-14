package com.bank.application.web.webMapper;

import com.bank.application.web.dto.CustomerDTO;
import com.bank.domain.model.Customer;

public class WebCustomerMapper {
    public CustomerDTO toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setBirthDate(customer.getBirthDate());
        dto.setAddress(customer.getAddress());
        dto.setIdentityNumber(customer.getIdentityNumber());
        dto.setRegistrationDate(customer.getRegistrationDate());
        dto.setActive(customer.isActive());

        return dto;
    }

    public Customer toDomain(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setCustomerId(dto.getCustomerId());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setBirthDate(dto.getBirthDate());
        customer.setAddress(dto.getAddress());
        customer.setIdentityNumber(dto.getIdentityNumber());
        customer.setRegistrationDate(dto.getRegistrationDate());
        customer.setActive(dto.isActive());

        return customer;
    }
}
