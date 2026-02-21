package com.bank.infrastructure.config;

import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import com.bank.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ValidationService validationService() {
        return new ValidationService();
    }

    @Bean
    public AccountService accountService(AccountRepository accountRepository,
                                         TransactionRepository transactionRepository,
                                         ValidationService validationService) {
        return new AccountService(accountRepository, transactionRepository, validationService);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository,
                                                 AccountService accountService) {
        return new TransactionService(transactionRepository, accountService);
    }

    @Bean
    public AuthService authService(AccountService accountService,
                                   TransactionRepository transactionRepository,
                                   AccountRepository accountRepository) {
        return new AuthService(accountService,transactionRepository,accountRepository);
    }

    @Bean
    public ExchangeService exchangeService(AccountService accountService,
                                           TransactionService transactionService) {
        return new ExchangeService(accountService, transactionService);
    }

    @Bean
    public InterestService interestService(AccountRepository accountRepository,
                                           TransactionRepository transactionRepository,
                                           AccountService accountService) {
        return new InterestService(accountRepository, transactionRepository, accountService);
    }
}
