package com.bank.application.console.menu;

import com.bank.domain.model.Account;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;


@Component
public class ClientMenu {

    private final Scanner consoleScanner;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public ClientMenu(Scanner consoleScanner, AccountService accountService,
                      TransactionService transactionService) {
        this.consoleScanner = consoleScanner;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public void display(Account account) {
        System.out.println("\n🏦 MENIU CLIENT - ÎN DEZVOLTARE");
        System.out.println("Cont: " + account.getAccountNumber());
        System.out.println("Proprietar: " + account.getOwner().getFullName());
        // Aici va fi implementat meniul complet pentru client
    }
}
