package com.bank.application.console.util;

import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Utilitar pentru citirea input-ului de la consolă.
 * Oferă metode validate pentru toate tipurile de date necesare.
 */
@Component
@Profile("console")
public class ConsoleReader {

    private final Scanner scanner;

    @Autowired
    public ConsoleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    // ============ CITIRE DE BAZĂ ============

    /**
     * Citește un șir de caractere (fără a fi gol)
     */
    public String readString(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("❌ Câmpul nu poate fi gol!");
            return readString(prompt);
        }
        return input;
    }

    /**
     * Citește o linie (poate fi goală)
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Citește un număr întreg
     */
    public int readInt(String prompt) {
        System.out.print(prompt);
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            return value;
        } catch (NumberFormatException e) {
            System.out.println("❌ Vă rugăm introduceți un număr valid!");
            return readInt(prompt);
        }
    }

    /**
     * Citește un număr întreg între limite
     */
    public int readIntInRange(String prompt, int min, int max) {
        int value = readInt(prompt);
        if (value < min || value > max) {
            System.out.printf("❌ Valoarea trebuie să fie între %d și %d!%n", min, max);
            return readIntInRange(prompt, min, max);
        }
        return value;
    }

    /**
     * Citește un număr zecimal (BigDecimal)
     */
    public BigDecimal readBigDecimal(String prompt) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim().replace(",", ".");
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Vă rugăm introduceți o sumă validă!");
            return readBigDecimal(prompt);
        }
    }

    /**
     * Citește parola (fără ecou - simplificat)
     */
    public String readPassword(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Așteaptă apăsarea tastei Enter
     */
    public void waitForEnter() {
        System.out.print("\n↵ Apăsați Enter pentru a continua...");
        scanner.nextLine();
    }

    // ============ VALIDĂRI SPECIFICE ============

    /**
     * Citește și validează număr cont (16 cifre)
     */
    public String readAccountNumber(String prompt) {
        String accountNumber = readString(prompt);

        // Elimină spații și liniuțe
        accountNumber = accountNumber.replaceAll("[\\s-]", "");

        if (accountNumber.length() != 16) {
            System.out.println("❌ Numărul contului trebuie să aibă 16 cifre!");
            return readAccountNumber(prompt);
        }

        if (!accountNumber.matches("\\d+")) {
            System.out.println("❌ Numărul contului trebuie să conțină doar cifre!");
            return readAccountNumber(prompt);
        }

        return accountNumber;
    }

    /**
     * Citește și validează sumă pozitivă
     */
    public BigDecimal readAmount(String prompt) {
        BigDecimal amount = readBigDecimal(prompt);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("❌ Suma trebuie să fie mai mare decât 0!");
            return readAmount(prompt);
        }

        return amount;
    }

    /**
     * Citește și validează sumă cu limită minimă
     */
    public BigDecimal readAmountMin(String prompt, BigDecimal min) {
        BigDecimal amount = readAmount(prompt);

        if (amount.compareTo(min) < 0) {
            System.out.printf("❌ Suma minimă este %s!%n", min);
            return readAmountMin(prompt, min);
        }

        return amount;
    }

    /**
     * Citește și validează cod valută (MDL, EUR, USD, GBP, RON)
     */
    public Currency readCurrency(String prompt) {
        String code = readString(prompt).toUpperCase();

        try {
            return Currency.fromCode(code);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Monedă invalidă! Valori permise: MDL, EUR, USD, GBP, RON");
            return readCurrency(prompt);
        }
    }

    /**
     * Citește și validează email
     */
    public String readEmail(String prompt) {
        String email = readString(prompt);
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!email.matches(emailRegex)) {
            System.out.println("❌ Adresă de email invalidă!");
            return readEmail(prompt);
        }

        return email;
    }

    /**
     * Citește și validează număr de telefon
     */
    public String readPhoneNumber(String prompt) {
        String phone = readString(prompt);
        String phoneRegex = "^[+]?[0-9]{10,15}$";

        if (!phone.matches(phoneRegex)) {
            System.out.println("❌ Număr de telefon invalid! (10-15 cifre, poate începe cu +)");
            return readPhoneNumber(prompt);
        }

        return phone;
    }

    /**
     * Confirmare Da/Nu
     */
    public boolean confirm(String prompt) {
        System.out.print(prompt + " (d/n): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.equals("d") || input.equals("da")) {
            return true;
        } else if (input.equals("n") || input.equals("nu")) {
            return false;
        } else {
            System.out.println("❌ Vă rugăm introduceți 'd' sau 'n'!");
            return confirm(prompt);
        }
    }

    /**
     * Citește o dată în format YYYY-MM-DD
     */
    public String readDate(String prompt) {
        String date = readString(prompt);
        String dateRegex = "^\\d{4}-\\d{2}-\\d{2}$";

        if (!date.matches(dateRegex)) {
            System.out.println("❌ Format invalid! Folosiți YYYY-MM-DD (ex: 2026-02-11)");
            return readDate(prompt);
        }

        return date;
    }
}
