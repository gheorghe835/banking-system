package com.bank.application.console.util;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Transaction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utilitar pentru afișare formatată în consolă.
 * Oferă metode pentru tabele, culori, mesaje și formatări.
 */
@Component
@Profile("console")
public class ConsolePrinter {

    // ============ CONSTANTE ============

    private static final String LINE_SEPARATOR = "=";
    private static final String SECTION_SEPARATOR = "-";
    private static final String TABLE_SEPARATOR = "─";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BOLD = "\u001B[1m";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // ============ MESAJE GENERALE ============

    /**
     * Afișează antet principal
     */
    public void printHeader(String title) {
        System.out.println("\n" + ANSI_BOLD + ANSI_BLUE + LINE_SEPARATOR.repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BOLD + " " + title + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + LINE_SEPARATOR.repeat(60) + ANSI_RESET + "\n");
    }

    /**
     * Afișează secțiune
     */
    public void printSection(String title) {
        System.out.println("\n" + ANSI_BOLD + ANSI_CYAN + SECTION_SEPARATOR.repeat(40) + ANSI_RESET);
        System.out.println(ANSI_BOLD + " " + title + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + SECTION_SEPARATOR.repeat(40) + ANSI_RESET);
    }

    /**
     * Afișează subsol
     */
    public void printFooter() {
        System.out.println(ANSI_BLUE + LINE_SEPARATOR.repeat(60) + ANSI_RESET + "\n");
    }

    /**
     * Afișează linie separatoare
     */
    public void separator() {
        System.out.println(ANSI_WHITE + SECTION_SEPARATOR.repeat(40) + ANSI_RESET);
    }

    /**
     * Afișează linie goală
     */
    public void newLine() {
        System.out.println();
    }

    /**
     * Afișează linie goală multiplă
     */
    public void newLine(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println();
        }
    }

    /**
     * Curăță ecranul
     */
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            newLine(30);
        }
    }

    // ============ MESAJE COLORATE ============

    /**
     * Mesaj de succes (verde)
     */
    public void printSuccess(String message) {
        System.out.println(ANSI_GREEN + "✅ " + message + ANSI_RESET);
    }

    /**
     * Mesaj de eroare (roșu)
     */
    public void printError(String message) {
        System.out.println(ANSI_RED + "❌ Eroare: " + message + ANSI_RESET);
    }

    /**
     * Mesaj de avertizare (galben)
     */
    public void printWarning(String message) {
        System.out.println(ANSI_YELLOW + "⚠️  " + message + ANSI_RESET);
    }

    /**
     * Mesaj informativ (albastru)
     */
    public void printInfo(String message) {
        System.out.println(ANSI_BLUE + "ℹ️  " + message + ANSI_RESET);
    }

    /**
     * Mesaj evidențiat (mov)
     */
    public void printHighlight(String message) {
        System.out.println(ANSI_PURPLE + ANSI_BOLD + message + ANSI_RESET);
    }

    // ============ FORMATĂRI ============

    /**
     * Formatează sumă în MDL
     */
    public String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * Formatează sumă cu monedă
     */
    public String formatCurrency(BigDecimal amount, Currency currency) {
        if (amount == null) return "0.00 " + currency;
        return amount.setScale(2, RoundingMode.HALF_UP) + " " + currency;
    }

    /**
     * Formatează număr cont (XX-XXXX-XX)
     */
    public String formatAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() != 16) {
            return accountNumber;
        }
        return accountNumber.substring(0, 2) + "-" +
                accountNumber.substring(2, 6) + "-" +
                accountNumber.substring(6, 8) + "-" +
                accountNumber.substring(8, 16);
    }

    /**
     * Formatează dată
     */
    public String formatDate(LocalDate date) {
        return date != null ? date.format(dateFormatter) : "N/A";
    }

    /**
     * Formatează dată și oră
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(dateTimeFormatter) : "N/A";
    }

    /**
     * Formatează procent
     */
    public String formatPercent(double value) {
        return String.format("%.2f%%", value);
    }

    // ============ AFIȘĂRI TABELARE ============

    /**
     * Afișează tabel cu conturi
     */
    public void printAccounts(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            printInfo("Nu există conturi de afișat.");
            return;
        }

        printSection("LISTĂ CONTURI");
        System.out.printf("%-20s %-25s %-12s %-15s %-15s%n",
                "Număr Cont", "Proprietar", "Tip", "Sold (MDL)", "Status");
        separator();

        for (Account account : accounts) {
            String status = account.isActive() ? "Activ" : "Inactiv";
            String statusColor = account.isActive() ? ANSI_GREEN : ANSI_RED;

            System.out.printf("%-20s %-25s %-12s %-15s %s%s%s%n",
                    formatAccountNumber(account.getAccountNumber()),
                    account.getOwner() != null ? account.getOwner().getFullName() : "N/A",
                    account.getAccountType(),
                    formatAmount(account.getBalance(Currency.MDL)) + " MDL",
                    statusColor, status, ANSI_RESET);
        }
        printFooter();
    }

    /**
     * Afișează tabel cu tranzacții
     */
    public void printTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            printInfo("Nu există tranzacții de afișat.");
            return;
        }

        printSection("ISTORIC TRANZACȚII");
        System.out.printf("%-10s %-20s %-15s %-15s %-25s %-12s%n",
                "ID", "Data", "Tip", "Sumă", "Descriere", "Status");
        separator();

        for (Transaction t : transactions) {
            String statusColor;
            switch (t.getStatus()) {
                case COMPLETED: statusColor = ANSI_GREEN; break;
                case FAILED: statusColor = ANSI_RED; break;
                case PENDING: statusColor = ANSI_YELLOW; break;
                default: statusColor = ANSI_WHITE;
            }

            System.out.printf("%-10s %-20s %-15s %-15s %-25s %s%s%s%n",
                    t.getTransactionId(),
                    formatDateTime(t.getTimestamp()),
                    t.getType(),
                    formatCurrency(t.getAmount(), t.getCurrency()),
                    truncate(t.getDescription(), 25),
                    statusColor, t.getStatus(), ANSI_RESET);
        }
        printFooter();
    }

    /**
     * Afișează tabel cu clienți
     */
    public void printCustomers(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            printInfo("Nu există clienți de afișat.");
            return;
        }

        printSection("LISTĂ CLIENȚI");
        System.out.printf("%-15s %-25s %-30s %-15s %-10s%n",
                "ID Client", "Nume", "Email", "Telefon", "Status");
        separator();

        for (Customer c : customers) {
            String status = c.isActive() ? "Activ" : "Inactiv";
            String statusColor = c.isActive() ? ANSI_GREEN : ANSI_RED;

            System.out.printf("%-15s %-25s %-30s %-15s %s%s%s%n",
                    c.getCustomerId(),
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhoneNumber() != null ? c.getPhoneNumber() : "N/A",
                    statusColor, status, ANSI_RESET);
        }
        printFooter();
    }

    /**
     * Afișează cursuri valutare
     */
    public void printExchangeRates(Map<Currency, BigDecimal> rates) {
        if (rates == null || rates.isEmpty()) {
            printError("Cursurile valutare nu sunt disponibile.");
            return;
        }

        printSection("CURS VALUTAR");
        System.out.printf("%-10s %-20s %-15s%n", "Moneda", "Cod", "Curs (MDL)");
        separator();

        for (Map.Entry<Currency, BigDecimal> entry : rates.entrySet()) {
            Currency currency = entry.getKey();
            if (currency != Currency.MDL) {
                System.out.printf("%-10s %-20s %-15s%n",
                        currency.getName(),
                        currency.getCode(),
                        entry.getValue().setScale(4, RoundingMode.HALF_UP));
            }
        }
        System.out.println("\n💡 1 MDL = 1.0000 MDL");
        printFooter();
    }

    /**
     * Afișează informații cont
     */
    public void printAccountDetails(Account account) {
        if (account == null) {
            printError("Cont inexistent.");
            return;
        }

        printSection("DETALII CONT");
        System.out.println("Număr cont:     " + ANSI_BOLD + formatAccountNumber(account.getAccountNumber()) + ANSI_RESET);
        System.out.println("Proprietar:     " + account.getOwner().getFullName());
        System.out.println("Tip cont:       " + account.getAccountType());
        System.out.println("Data creării:   " + formatDate(account.getCreationDate()));
        System.out.println("Status:         " + (account.isActive() ? ANSI_GREEN + "Activ" + ANSI_RESET : ANSI_RED + "Inactiv" + ANSI_RESET));
        System.out.println("Limită zilnică: " + formatCurrency(account.getDailyWithdrawalLimit(), Currency.MDL));

        printSection("SOLDURI");
        for (Currency currency : Currency.values()) {
            BigDecimal balance = account.getBalance(currency);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("%-4s: %s%n", currency, formatCurrency(balance, currency));
            }
        }

        System.out.println("\nTotal în MDL:   " + ANSI_BOLD + formatCurrency(account.getTotalBalanceInMDL(), Currency.MDL) + ANSI_RESET);
        separator();
    }

    // ============ METODE UTILITARE ============

    /**
     * Trunchiază text la lungime maximă
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Afișează progres bara (simplificat)
     */
    public void printProgress(int current, int total, String message) {
        int percent = (int) ((double) current / total * 100);
        int barLength = 30;
        int filledLength = (int) ((double) barLength * current / total);

        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ");
        bar.append(percent).append("%");

        System.out.print("\r" + message + " " + bar.toString());
        if (current == total) {
            System.out.println();
        }
    }

    /**
     * Afișează o linie de tabel
     */
    public void printTableRow(Object... columns) {
        StringBuilder row = new StringBuilder();
        for (Object col : columns) {
            row.append(String.format("%-20s", col));
        }
        System.out.println(row.toString());
    }
}
