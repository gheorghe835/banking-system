package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingApplication {
    public static void main(String[] args) {
        System.out.println("🏦 Starting Banking System...");
        SpringApplication.run(BankingApplication.class, args);
    }
}
