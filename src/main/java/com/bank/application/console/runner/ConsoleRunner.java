package com.bank.application.console.runner;

import com.bank.application.console.menu.MainMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * Component care pornește interfața consolă atunci când aplicația rulează.
 * Rulează doar când profilul "console" este activ.
 */
@Component
@Profile("console")
public class ConsoleRunner implements CommandLineRunner {

    private final MainMenu mainMenu;

    @Autowired
    public ConsoleRunner(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" 🏦 BANCA COMERCIALĂ - SISTEM BANCAR SPRING BOOT");
        System.out.println("=".repeat(60));
        System.out.println(" Versiune: 1.0.0");
        System.out.println(" Database: MySQL");
        System.out.println(" Mod: Console Interface");
        System.out.println("=".repeat(60) + "\n");

        // Pornește meniul principal
        mainMenu.display();
    }

    /*
    @Override
    public void run(String... args) {
        System.out.println("🔥🔥🔥 CONSOLE RUNNER A PORNIIT! 🔥🔥🔥");
        System.out.println("Apasă Enter pentru a continua...");
        new Scanner(System.in).nextLine();
    }*/
}
