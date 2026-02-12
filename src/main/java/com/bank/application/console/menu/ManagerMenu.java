package com.bank.application.console.menu;

import com.bank.domain.model.BankManager;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ManagerMenu {
    private final Scanner scanner;
    private final AuthService authService;
    private final AccountService accountService;

    @Autowired
    public ManagerMenu(Scanner scanner,
                       AuthService authService,
                       AccountService accountService) {
        this.scanner = scanner;
        this.authService = authService;
        this.accountService = accountService;
    }


    public void display(BankManager manager) {  // ← CU parametru!
        System.out.println("\n📊 MENIU MANAGER - ÎN DEZVOLTARE");
        System.out.println("Manager: " + manager.getFullName());
        System.out.println("Nivel acces: " + manager.getAccessLevel());
        // Aici va fi implementat meniul complet pentru manager
    }
}
