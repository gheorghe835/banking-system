package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication
public class BankingApplication {
    public static void main(String[] args) {
        System.out.println("🏦 Starting Banking System...");
        SpringApplication.run(BankingApplication.class, args);
    }
}
