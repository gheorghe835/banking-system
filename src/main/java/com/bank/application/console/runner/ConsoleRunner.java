package com.bank.application.console.runner;

import com.bank.application.console.menu.MainMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
    public void run(String... args) {
        // Pornește meniul principal
        mainMenu.display();
    }
}