package com.bank.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Scanner;

@Configuration
//@Profile("console")
public class ConsoleConfig {

    @Bean
    public Scanner scanner() {
        return new Scanner(System.in);
    }
}

