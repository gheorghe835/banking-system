package com.bank.application.web.controller;

import com.bank.domain.model.Customer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class TestJsonController {

    @GetMapping("/customer")
    public Customer testCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("TEST001");
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setRegistrationDate(LocalDateTime.now());
        return customer;
    }
}
