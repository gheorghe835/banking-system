package com.bank.application.console.menu;

import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.BankingSecurityException;
import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AuthService;
import com.bank.domain.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

/**
 * Meniul principal al băncii.
 * Afișează ecranul de bun venit și direcționează către autentificare.
 */
@Component
@Profile("console")
public class MainMenu {

    private final Scanner scanner;
    private final AuthService authService;
    private final ExchangeService exchangeService;
    private final ClientMenu clientMenu;
    private final ManagerMenu managerMenu;

    @Autowired
    public MainMenu(Scanner scanner,
                    AuthService authService,
                    ExchangeService exchangeService,
                    ClientMenu clientMenu,
                    ManagerMenu managerMenu) {
        this.scanner = scanner;
        this.authService = authService;
        this.exchangeService = exchangeService;
        this.clientMenu = clientMenu;
        this.managerMenu = managerMenu;
    }

    /**
     * Afișează și gestionează meniul principal.
     */
    public void display() {
        boolean running = true;

        while (running) {
            displayWelcomeScreen();

            System.out.print("\n👉 Alegeți o opțiune: ");

            try {
                int option = scanner.nextInt();
                scanner.nextLine(); // Consumă newline

                switch (option) {
                    case 1:
                        authenticateClient();
                        break;
                    case 2:
                        authenticateManager();
                        break;
                    case 3:
                        displayExchangeRates();
                        break;
                    case 4:
                        displayContactInfo();
                        break;
                    case 5:
                        System.out.println("\n🙏 Mulțumim că ați vizitat Banca Comercială. O zi frumoasă!");
                        running = false;
                        break;
                    default:
                        System.out.println("\n❌ Opțiune invalidă! Vă rugăm să alegeți din nou.");
                }

                if (running && (option >= 1 && option <= 4)) {
                    System.out.print("\n↵ Apăsați Enter pentru a continua...");
                    scanner.nextLine();
                }

            } catch (Exception e) {
                System.out.println("\n⚠️  Eroare: Vă rugăm să introduceți un număr valid.");
                scanner.nextLine(); // Curăță buffer-ul
            }
        }

        System.out.println("\n👋 Aplicația se închide...");
    }

    /**
     * Afișează ecranul de bun venit.
     */
    private void displayWelcomeScreen() {
        clearScreen();

        System.out.println("\n" + "=".repeat(60));
        System.out.println(" 🏦 BANCA COMERCIALĂ");
        System.out.println("=".repeat(60));
        System.out.printf(" 📅 Data: %s%n", LocalDate.now());
        System.out.printf(" 🕐 Ora: %s%n", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        System.out.println("=".repeat(60));
        System.out.println("\n📋 MENIU PRINCIPAL:");
        System.out.println(" 1. 👤 Autentificare Client");
        System.out.println(" 2. 👔 Autentificare Manager");
        System.out.println(" 3. 💱 Curs Valutar");
        System.out.println(" 4. 📞 Contact");
        System.out.println(" 5. 🚪 Ieșire");
        System.out.println("\n" + "=".repeat(60));
    }

    /**
     * Autentifică un client.
     */
    private void authenticateClient() {
        System.out.println("\n" + "═".repeat(40));
        System.out.println(" AUTENTIFICARE CLIENT");
        System.out.println("═".repeat(40));

        System.out.print("\n Număr cont (16 cifre): ");
        String accountNumber = scanner.nextLine().trim();

        if (accountNumber.isEmpty()) {
            System.out.println("\n❌ Eroare: Numărul contului nu poate fi gol!");
            return;
        }

        System.out.print(" Parola: ");
        String password = scanner.nextLine();

        try {
            // Autentificare folosind AuthService - returnează Account
            Account account = authService.authenticateClient(accountNumber, password);

            System.out.println("\n✅ Autentificare reușită!");
            System.out.println("   Bun venit, " + account.getOwner().getFullName() + "!");
            System.out.println("   Cont: " + account.getAccountNumber());
            System.out.println("   Tip cont: " + account.getAccountType());

            // Deschide meniul clientului - transmite Account, nu Customer
            clientMenu.display(account);

        } catch (AccountNotFoundException e) {
            System.out.println("\n❌ Eroare: Contul cu numărul " + accountNumber + " nu există!");
        } catch (BankingSecurityException e) {
            if (e.getMessage().contains("blocat")) {
                System.out.println("\n🔒 Cont blocat! " + e.getMessage());
            } else {
                System.out.println("\n🔒 Autentificare eșuată: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("\n❌ Eroare autentificare: " + e.getMessage());
        }
    }

    /**
     * Autentifică un manager.
     */
    private void authenticateManager() {
        System.out.println("\n" + "═".repeat(40));
        System.out.println(" 👔 AUTENTIFICARE MANAGER");
        System.out.println("═".repeat(40));

        System.out.print("\n🔑 Utilizator: ");
        String username = scanner.nextLine().trim();

        System.out.print("🔒 Parolă: ");
        String password = scanner.nextLine();

        try {
            // Autentificare manager
            BankManager manager = authService.authenticateManager(username, password);

            System.out.println("\n✅ Autentificare reușită!");
            System.out.println("   Bun venit, " + manager.getFullName() + "!");
            System.out.println("   Nivel acces: " + manager.getAccessLevel());

            // Deschide meniul managerului - ACUM DECOMENTAT ȘI IMPLEMENTAT
            managerMenu.display(manager);

        } catch (BankingSecurityException e) {
            System.out.println("\n❌ Autentificare eșuată: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Eroare autentificare: " + e.getMessage());
        }
    }

    /**
     * Afișează cursurile valutare.
     */
    private void displayExchangeRates() {
        System.out.println("\n" + "─".repeat(40));
        System.out.println(" 💱 CURS VALUTAR");
        System.out.println("─".repeat(40));

        try {
            // Preluare rate din ExchangeService
            Map<Currency, BigDecimal> rates = exchangeService.getAllExchangeRates();

            System.out.println(" Moneda │ Cod │ Curs (MDL)");
            System.out.println(" ───────┼─────┼───────────");

            for (Map.Entry<Currency, BigDecimal> entry : rates.entrySet()) {
                Currency currency = entry.getKey();
                BigDecimal rate = entry.getValue();

                // Nu afișăm MDL (rata 1.0)
                if (currency != Currency.MDL) {
                    System.out.printf(" %-6s │ %-3s │ %.4f%n",
                            currency.getName(), currency.getCode(), rate);
                }
            }

            System.out.println("─".repeat(40));
            System.out.println(" 💡 Exemplu: 100 EUR = " +
                    exchangeService.convertToMDL(BigDecimal.valueOf(100), Currency.EUR).setScale(2) + " MDL");

        } catch (Exception e) {
            // Fallback la rate hardcodate dacă serviciul nu e disponibil
            displayFallbackExchangeRates();
        }
    }

    /**
     * Afișează cursuri valutare de rezervă (hardcodate).
     */
    private void displayFallbackExchangeRates() {
        System.out.println(" Moneda │ Cod │ Curs (MDL)");
        System.out.println(" ───────┼─────┼───────────");
        System.out.println(" Euro   │ EUR │ 19.4500");
        System.out.println(" Dolar  │ USD │ 17.5500");
        System.out.println(" Liră   │ GBP │ 22.1000");
        System.out.println(" Leu R. │ RON │ 4.0000");
        System.out.println("─".repeat(40));
        System.out.println(" ℹ️  Cursuri indicative. Pentru cursul oficial, contactați banca.");
    }

    /**
     * Afișează informațiile de contact.
     */
    private void displayContactInfo() {
        System.out.println("\n" + "─".repeat(40));
        System.out.println(" 📞 CONTACT");
        System.out.println("─".repeat(40));

        System.out.println("\n🏢 Banca Comercială S.A.");
        System.out.println("📍 Str. Independenței 1, Chișinău, Moldova");
        System.out.println("📞 Telefon: 022 123 456");
        System.out.println("📱 Mobil: 0600 12345");
        System.out.println("✉️  Email: info@bancacomerciala.md");
        System.out.println("🌐 Website: www.bancacomerciala.md");
        System.out.println("\n⏰ Program:");
        System.out.println("   Luni-Vineri: 09:00 - 18:00");
        System.out.println("   Sâmbătă: 09:00 - 14:00");
        System.out.println("   Duminică: Închis");
    }

    /**
     * Curăță ecranul consolei.
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("\n".repeat(30));
        }
    }
}