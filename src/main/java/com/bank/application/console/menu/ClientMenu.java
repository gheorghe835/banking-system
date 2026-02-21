package com.bank.application.console.menu;

import com.bank.application.console.util.ConsolePrinter;
import com.bank.application.console.util.ConsoleReader;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;


@Component
@Profile("console")
public class ClientMenu {

    private final ConsoleReader reader;
    private final ConsolePrinter printer;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public  ClientMenu(ConsoleReader reader,
                       ConsolePrinter printer,
                       AccountService accountService,
                       TransactionService transactionService){
        this.reader = reader;
        this.printer = printer;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public void display(Account account){
        openClientMenu(account);
    }



    public void openClientMenu(Account account) {
        boolean running = true;

        while (running) {
            printer.printSection("MENIU CLIENT");
            printer.printInfo("Cont: " + account.getAccountNumber());
            printer.printInfo("Proprietar: " + account.getOwner().getFullName());
            printer.newLine();

            printer.printInfo("1. 👁️  Informatii cont");
            printer.printInfo("2. 💰 Solduri si tranzactii");
            printer.printInfo("3. 📥 Depunere");
            printer.printInfo("4. 📤 Retragere");
            printer.printInfo("5. 🔄 Transfer");
            printer.printInfo("6. 💱 Schimb valutar");
            printer.printInfo("7. ⚙️  Setari cont");
            printer.printInfo("8. 🚪 Deconectare");
            printer.separator();

            int option = reader.readIntInRange("\n👉 Alegeti optiunea: ", 1, 8);


                switch (option) {
                    case 1:
                        displayAccountInfo(account);
                        break;

                    case 2:
                        displayBalanceMenu(account);
                        break;

                    case 3:
                        performDeposit(account);
                        break;

                    case 4:
                        performWithdrawal(account);
                        break;

                    case 5:
                        performTransfer(account);
                        break;

                    case 6:
                        performCurrencyExchange(account);
                        break;

                    case 7:
                        openAccountSettings(account);
                        break;

                    case 8:
                        printer.printSuccess("\n👋 Deconectare reusita! Vă asteptam din nou!");
                        running = false;
                        break;

                    default:
                        System.out.println("Optiune invalida.");
                }

                if (running && option != 8){
                    reader.waitForEnter();
                }

        }
    }

    private void displayAccountInfo(Account account){
        printer.printSection("INFORMATII CONT");
        printer.printAccountDetails(account);
    }
    private void displayBalanceMenu(Account account) {
        boolean viewing = true;

        while (viewing) {
            printer.printSection("SOLDURI SI TRANZACTII");
            printer.printInfo("1. 👁️  Afisare solduri");
            printer.printInfo("2. 📋 Istoric tranzactii");
            printer.printInfo("3. 📊 Extras de cont");
            printer.printInfo("4. 💱 Total în MDL");
            printer.printInfo("5. ↩️  Inapoi");
            printer.separator();

            int choice = reader.readIntInRange("\n👉 Alegeti optiunea: ", 1, 5);

            switch (choice) {
                case 1:
                    displayBalances(account);
                    break;

                case 2:
                    displayTransactionHistory(account);
                    break;

                case 3:
                    generateAccountStatement(account);
                    break;

                case 4:
                    displayTotalInMDL(account);
                    break;

                case 5:
                    viewing = false;
                    break;
            }
            if (viewing && choice != 5) {
                reader.waitForEnter();
            }
        }
    }

    private void displayBalances(Account account){
        printer.printSection("SOLDURI");
        for (Currency currency : Currency.values()){
            BigDecimal balance = account.getBalance(currency);
            if (balance.compareTo(BigDecimal.ZERO) > 0){
                printer.printInfo(currency.getCode() + ": " + printer.formatCurrency(balance,currency));
            }
        }
    }

    private void displayTransactionHistory(Account account){
        int limit = reader.readIntInRange("Numar de tranzactii de afisat(1 - 50):",1,50);

        var transactions = transactionService.getLastTransactions(account.getAccountNumber(),limit);

        printer.printTransactions(transactions);
    }

    private void generateAccountStatement(Account account){
        try {
            printer.printInfo("Format data: AAAA-LL-ZZ ");
            String fromDateStr = reader.readString("Data de inceput: ");
            String toDateStr = reader.readString("Data de sfirsit:");

            LocalDate fromDate = LocalDate.parse(fromDateStr);
            LocalDate toDate = LocalDate.parse(toDateStr);

            var transactions = transactionService.generateAccountStatement(
                    account.getAccountNumber(),
                    fromDate.atStartOfDay(),
                    toDate.atTime(23,59,59)
            );

            printer.printSection("EXTRAS DE CONT");
            printer.printInfo("Perioada: " + printer.formatDate(fromDate) + " - " + printer.formatDate(toDate));
            printer.printTransactions(transactions);
        }
        catch (DateTimeParseException e){
            printer.printError("Format data invalid! Folositi: AAAA-LL-ZZ");
        }
    }

    private void displayTotalInMDL(Account account){
        BigDecimal total = account.getTotalBalanceInMDL();
        printer.printSuccess("Toatal in MDL; " + printer.formatCurrency(total,Currency.MDL));
    }

    private void performDeposit(Account account){
        printer.printSection("   📥 DEPUNERE");

        Currency currency = reader.readCurrency("Moneda (MDL/EUR/USD/GBP/RON):");
        BigDecimal amount = reader.readAmount("Suma de depus: ");

        try {
            accountService.deposit(account.getAccountNumber(),amount,currency);
            printer.printSuccess("Depunere finalizata cu succes");
            printer.printInfo("Sold nou " + currency.getCode() + ": " + printer.formatCurrency(account.getBalance(currency),currency));
        }
        catch (Exception e){
            printer.printError("Depunere esuata: " + e.getMessage());
        }
    }

    private void performWithdrawal(Account account){
        printer.printSection("   📤 RETRAGERE");
        Currency currency = reader.readCurrency("Moneda (MDL/EUR/USD/GBP/RON):");
        BigDecimal amount = reader.readAmount("Suma:");

        try {
            accountService.withdraw(account.getAccountNumber(),amount,currency);
            printer.printSuccess("Retragere finalizata cu succes");
            printer.printInfo("Sold nou " + currency.getCode() + ": " + printer.formatCurrency(account.getBalance(currency),currency));
        }
        catch (Exception e){
            printer.printError("Retragere esuata: " + e.getMessage());
        }
    }

    private void performTransfer(Account account){
        printer.printSection("   🔄 TRANSFER");

        String targetAccountNumber = reader.readAccountNumber("Catre contul(16 cifre): ");
        String sourceAccountNumber = account.getAccountNumber();

        Currency currency = reader.readCurrency("Moneda: ");
        BigDecimal amount = reader.readAmount("Suma de transferat: ");
        String description = reader.readString("Descriere: ");

        try {
            accountService.transfer(
                    sourceAccountNumber,
                    targetAccountNumber,
                    amount,
                    currency,
                    description.isEmpty() ? "Transfer bancar" : description
            );
            printer.printSuccess("Transfer finalizat cu succes");
        }
        catch (Exception e){
            printer.printError("Transfer esuat: " + e.getMessage());
        }
    }

    private void performCurrencyExchange(Account account){
        printer.printSection("💱 SCHIMB VALUTAR");

        Currency fromCurrency = reader.readCurrency("Din moneda: ");
        Currency toCurrency = reader.readCurrency("In moneda: ");
        BigDecimal amount = reader.readAmount("Suma de schimbat: ");

        try {
            printer.printSuccess("Schimb valutar finalizat.");
        } catch (Exception e) {
            printer.printError("Schimb valutar eșuat: " + e.getMessage());
        }
    }

    private void openAccountSettings(Account account){
        boolean configuring = true;

        while (configuring) {
            printer.printSection("⚙️  SETARI CONT");
            printer.printInfo("1. 🔐 Schimbare parola");
            printer.printInfo("2. ⚖️  Setare limita retragere");
            printer.printInfo("3. 🔴 Dezactivare cont");
            printer.printInfo("4. 🟢 Reactivare cont");
            printer.printInfo("5. ↩️  Inapoi");
            printer.separator();

            int choice = reader.readIntInRange("\n👉 Alegeti optiunea: ", 1, 5);

            switch (choice) {
                case 1:
                    changePassword(account);
                    break;

                case 2:
                    setWithdrawalLimit(account);
                    break;

                case 3:
                    deactivateAccount(account);
                    break;

                case 4:
                    reactivateAccount(account);
                    break;

                case 5:
                    configuring = false;
                    break;
            }
        }
    }

    private void changePassword(Account account){
        printer.printSection("🔐 SCHIMBARE PAROLA");

        String oldPassword = reader.readPassword("Parola actuala: ");
        String newPassword = reader.readPassword("Parola noua: ");
        String confirmPassword = reader.readPassword("Confirma parola noua: ");

        if (!newPassword.equals(confirmPassword)){
            printer.printError("Parolele nu coincid");
            return;
        }

        try {
            printer.printSuccess("Parola a fost schimbata cu succes");
        }
        catch (Exception e){
            printer.printError("Schimbare parola esuata: " + e.getMessage());
        }
    }

    private void setWithdrawalLimit(Account account){
        printer.printSection("⚖️  LIMITĂ RETRAGERE ZILNICĂ");

        printer.printInfo("Limita actuala: " + printer.formatCurrency(account.getDailyWithdrawalLimit(),Currency.MDL));

        BigDecimal newLimit = reader.readAmountMin("Noua limita(MDL): ",BigDecimal.valueOf(100));

        try {
            accountService.updateDailyWithdrawalLimit(account.getAccountNumber(), newLimit);
            printer.printSuccess("Limita a fost actualizata cu succes");
        }
        catch (Exception e){
            printer.printError("Actualizare limita esuata: " + e.getMessage());
        }
    }

    private void deactivateAccount(Account account){
        printer.printSection("🔴 DEZACTIVARE CONT");
        printer.printWarning("ATENȚIE! Această operațiune va dezactiva contul temporar.");

        if (reader.confirm("Sigur doriti sa dezactivati contul?")){
            try {
                accountService.blockAccount(account.getAccountNumber());
                printer.printSuccess("Contul a fost dezactivat cu succes");
            }
            catch (Exception e){
                printer.printInfo("Dezactivare esuata: " + e.getMessage());
            }
        }
        else {
            printer.printInfo("Operatiune anualata");
        }
    }

    private void reactivateAccount(Account account){
        printer.printSection("🟢 REACTIVARE CONT");

        if (reader.confirm("Sigur doriți să reactivați contul?")) {
            try {
                accountService.unblockAccount(account.getAccountNumber());
                printer.printSuccess("Contul a fost reactivat cu succes");
            }
            catch (Exception e){
                printer.printError("Reactivare esuata: " + e.getMessage());
            }
        }
        else {
            printer.printInfo("Operatiune anualata");
        }
    }

}

