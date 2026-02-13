package com.bank.application.console.menu;

import com.bank.application.console.util.ConsolePrinter;
import com.bank.application.console.util.ConsoleReader;
import com.bank.domain.exception.DuplicateCustomerException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Component
@Profile("console")
public class ManagerMenu {

    private final ConsoleReader reader;
    private final ConsolePrinter printer;
    private final AccountService accountService;
    private final AuthService authService;
    private final ExchangeService exchangeService;
    private final InterestService interestService;
    private final TransactionService transactionService;
    private final CustomerService customerService;

    @Autowired
    public ManagerMenu(ConsoleReader reader,
                       ConsolePrinter printer,
                       AccountService accountService,
                       AuthService authService,
                       ExchangeService exchangeService,
                       InterestService interestService,
                       TransactionService transactionService,
                       CustomerService customerService) {
        this.reader = reader;
        this.printer = printer;
        this.accountService = accountService;
        this.authService = authService;
        this.exchangeService = exchangeService;
        this.interestService = interestService;
        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    public void display(BankManager manager) {
        printer.printSuccess("Bun venit, " + manager.getFullName() + "!");
        printer.printInfo("Nivel acces: " + manager.getAccessLevel());
        openManagerMenu(manager);
    }

    private void openManagerMenu(BankManager manager) {
        boolean running = true;

        while (running) {
            printer.printSection("MENIU MANAGER");
            printer.printInfo("Manager: " + manager.getFullName());
            printer.newLine();

            printer.printInfo("1.  ➕ Adăugare cont nou");
            printer.printInfo("2.  👁️  Afișare toate conturile");
            printer.printInfo("3.  🔍 Căutare cont");
            printer.printInfo("4.  🗑️  Ștergere cont");
            printer.printInfo("5.  🔒 Blocare cont");
            printer.printInfo("6.  🔓 Deblocare cont");
            printer.printInfo("7.  💱 Actualizare curs valutar");
            printer.printInfo("8.  📈 Raport solduri totale");
            printer.printInfo("9.  🏦 Aplicare dobândă");
            printer.printInfo("10. 🔴 Conturi inactive");
            printer.printInfo("11. 📊 Statistici tranzacții");
            printer.printInfo("12. 👥 Gestiune clienți");
            printer.printInfo("13. 🚪 Deconectare");
            printer.separator();

            int option = reader.readIntInRange("\n👉 Alegeți opțiunea: ", 1, 13);

            switch (option) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    displayAllAccounts();
                    break;
                case 3:
                    searchAccount();
                    break;
                case 4:
                    deleteAccount();
                    break;
                case 5:
                    blockAccount();
                    break;
                case 6:
                    unblockAccount();
                    break;
                case 7:
                    updateExchangeRates();
                    break;
                case 8:
                    generateBalanceReport();
                    break;
                case 9:
                    applyInterest();
                    break;
                case 10:
                    displayInactiveAccounts();
                    break;
                case 11:
                    displayTransactionStatistics();
                    break;
                case 12:
                    customerManagement();
                    break;
                case 13:
                    printer.printSuccess("Deconectare reușită!");
                    running = false;
                    break;
            }

            if (running && option != 13) {
                reader.waitForEnter();
            }
        }
    }

    // ============ 1. CREARE CONT NOU ============

    private void createAccount() {
        printer.printSection("CREARE CONT NOU");

        String accountNumber = reader.readAccountNumber("Număr cont (16 cifre): ");

        // Verifică dacă există deja
        try {
            accountService.findAccount(accountNumber);
            printer.printError("Contul există deja!");
            return;
        } catch (Exception e) {
            // Contul nu există - putem continua
        }

        String firstName = reader.readString("Prenume client: ");
        String lastName = reader.readString("Nume client: ");
        String email = reader.readEmail("Email: ");
        String phone = reader.readPhoneNumber("Telefon: ");

        printer.printInfo("Tip cont: 1. CURRENT  2. SAVINGS  3. BUSINESS");
        int typeOption = reader.readIntInRange("Alegeți tipul: ", 1, 3);
        String accountType;
        switch (typeOption) {
            case 1: accountType = Account.ACCOUNT_TYPE_CURRENT; break;
            case 2: accountType = Account.ACCOUNT_TYPE_SAVINGS; break;
            case 3: accountType = Account.ACCOUNT_TYPE_BUSINESS; break;
            default: accountType = Account.ACCOUNT_TYPE_CURRENT;
        }

        BigDecimal initialBalance = reader.readAmount("Sold inițial (MDL): ");

        try {
            // TODO: Creează mai întâi customer, apoi cont
            // Pentru moment, folosim un customer simplificat
            Customer customer = new Customer(firstName, lastName, email, phone, LocalDate.now().minusYears(20), "ID" + System.currentTimeMillis());

            Account account = accountService.createAccount(accountNumber, customer, accountType, initialBalance);
            printer.printSuccess("Cont creat cu succes!");
            printer.printAccountDetails(account);
        } catch (Exception e) {
            printer.printError("Eroare la crearea contului: " + e.getMessage());
        }
    }

    // ============ 2. AFIȘARE TOATE CONTURILE ============

    private void displayAllAccounts() {
        printer.printSection("TOATE CONTURILE");
        List<Account> accounts = accountService.getAllAccounts();
        printer.printAccounts(accounts);
        printer.printInfo("Total conturi: " + accounts.size());
    }

    // ============ 3. CĂUTARE CONT ============

    private void searchAccount() {
        printer.printSection("CĂUTARE CONT");
        String accountNumber = reader.readAccountNumber("Număr cont: ");

        try {
            Account account = accountService.findAccount(accountNumber);
            printer.printAccountDetails(account);
        } catch (Exception e) {
            printer.printError("Cont negăsit: " + e.getMessage());
        }
    }

    // ============ 4. ȘTERGERE CONT ============

    private void deleteAccount() {
        printer.printSection("ȘTERGERE CONT");
        printer.printWarning("ATENȚIE! Contul va fi șters definitiv!");

        String accountNumber = reader.readAccountNumber("Număr cont: ");

        try {
            Account account = accountService.findAccount(accountNumber);
            printer.printAccountDetails(account);

            if (reader.confirm("Sigur doriți să ștergeți acest cont?")) {
                boolean deleted = accountService.deleteAccount(accountNumber);
                if (deleted) {
                    printer.printSuccess("Cont șters cu succes!");
                }
            } else {
                printer.printInfo("Operațiune anulată.");
            }
        } catch (Exception e) {
            printer.printError("Eroare la ștergere: " + e.getMessage());
        }
    }

    // ============ 5. BLOCARE CONT ============

    private void blockAccount() {
        printer.printSection("BLOCARE CONT");
        String accountNumber = reader.readAccountNumber("Număr cont: ");

        try {
            Account account = accountService.blockAccount(accountNumber);
            printer.printSuccess("Cont blocat cu succes!");
            printer.printInfo("Status: Inactiv");
        } catch (Exception e) {
            printer.printError("Eroare la blocare: " + e.getMessage());
        }
    }

    // ============ 6. DEBLOCARE CONT ============

    private void unblockAccount() {
        printer.printSection("DEBLOCARE CONT");
        String accountNumber = reader.readAccountNumber("Număr cont: ");

        try {
            Account account = accountService.unblockAccount(accountNumber);
            printer.printSuccess("Cont deblocat cu succes!");
            printer.printInfo("Status: Activ");
        } catch (Exception e) {
            printer.printError("Eroare la deblocare: " + e.getMessage());
        }
    }

    // ============ 7. ACTUALIZARE CURS VALUTAR ============

    private void updateExchangeRates() {
        printer.printSection("ACTUALIZARE CURS VALUTAR");

        Map<Currency, BigDecimal> currentRates = exchangeService.getAllExchangeRates();

        for (Currency currency : Currency.values()) {
            if (currency != Currency.MDL) {
                BigDecimal currentRate = currentRates.get(currency);
                printer.printInfo(currency.getCode() + ": " +
                        (currentRate != null ? currentRate.setScale(4) : "N/A") + " MDL");

                BigDecimal newRate = reader.readBigDecimal("Noul curs pentru " + currency.getCode() + ": ");
                exchangeService.updateExchangeRate(currency, newRate);
            }
        }

        printer.printSuccess("Cursul valutar a fost actualizat!");
        displayExchangeRates();
    }

    private void displayExchangeRates() {
        printer.printExchangeRates(exchangeService.getAllExchangeRates());
    }

    // ============ 8. RAPORT SOLDURI TOTALE ============

    private void generateBalanceReport() {
        printer.printSection("RAPORT SOLDURI TOTALE");

        BigDecimal totalBalance = accountService.getTotalBankBalance();
        long totalAccounts = accountService.getTotalAccountCount();
        long activeAccounts = accountService.getActiveAccountCount();

        printer.printInfo("Total conturi: " + totalAccounts);
        printer.printInfo("Conturi active: " + activeAccounts);
        printer.printInfo("Conturi inactive: " + (totalAccounts - activeAccounts));
        printer.printSuccess("Sold total banca: " + printer.formatCurrency(totalBalance, Currency.MDL));

        // Sold pe valute
        // Aceasta metodă necesită extindere în AccountService
    }

    // ============ 9. APLICARE DOBÂNDĂ ============

    private void applyInterest() {
        printer.printSection("APLICARE DOBÂNDĂ");

        printer.printInfo("1. Aplicare dobândă zilnică");
        printer.printInfo("2. Aplicare dobândă personalizată");

        int choice = reader.readIntInRange("Alegeți: ", 1, 2);

        if (choice == 1) {
            interestService.applyInterestToAllAccounts();
            printer.printSuccess("Dobânda zilnică a fost aplicată!");
        } else {
            printer.printInfo("Perioada (zile): ");
            int days = reader.readInt("Număr zile: ");
            interestService.applyInterestForPeriod(LocalDate.now().minusDays(days), LocalDate.now());
        }
    }

    // ============ 10. CONTURI INACTIVE ============

    private void displayInactiveAccounts() {
        printer.printSection("CONTURI INACTIVE");
        List<Account> inactiveAccounts = accountService.getInactiveAccounts();

        if (inactiveAccounts.isEmpty()) {
            printer.printInfo("Nu există conturi inactive.");
        } else {
            printer.printAccounts(inactiveAccounts);
            printer.printWarning("Total conturi inactive: " + inactiveAccounts.size());
        }
    }

    // ============ 11. STATISTICI TRANZACȚII ============

    private void displayTransactionStatistics() {
        printer.printSection("STATISTICI TRANZACȚII");

        long totalTransactions = transactionService.getTotalTransactionCount();
        BigDecimal totalAmount = transactionService.getTotalTransactionAmount();

        printer.printInfo("Total tranzacții: " + totalTransactions);
        printer.printInfo("Suma totală tranzacționată: " + printer.formatCurrency(totalAmount, Currency.MDL));

        // Top 5 conturi după activitate
        // Necesită metodă nouă în TransactionService
    }

    // ============ 12. GESTIUNE CLIENȚI ============

    private void customerManagement() {
        boolean managing = true;

        while (managing) {
            printer.printSection("GESTIUNE CLIENȚI");
            printer.printInfo("1. 📋 Listă clienți");
            printer.printInfo("2. 🔍 Căutare client");
            printer.printInfo("3. ➕ Adăugare client");
            printer.printInfo("4. ✏️  Editare client");
            printer.printInfo("5. 🔒 Activare/Dezactivare client");
            printer.printInfo("6. 📊 Statistici clienți");
            printer.printInfo("7. ↩️  Înapoi");
            printer.separator();

            int choice = reader.readIntInRange("\n👉 Alegeți: ", 1, 7);

            switch (choice) {
                case 1:
                    displayAllCustomers();
                    break;
                case 2:
                    searchCustomer();
                    break;
                case 3:
                    addCustomer();
                    break;
                case 4:
                    editCustomer();
                    break;
                case 5:
                    toggleCustomerStatus();
                    break;
                case 6:
                    displayCustomerStatistics();
                    break;
                case 7:
                    managing = false;
                    break;
            }

            if (managing && choice != 7) {
                reader.waitForEnter();
            }
        }
    }

    private void displayAllCustomers() {
        printer.printSection("LISTĂ CLIENȚI");
        List<Customer> customers = customerService.getAllCustomers();
        printer.printCustomers(customers);
        printer.printInfo("Total clienți: " + customers.size());
    }

    private void searchCustomer() {
        printer.printSection("CĂUTARE CLIENT");
        printer.printInfo("1. Căutare după ID");
        printer.printInfo("2. Căutare după email");
        printer.printInfo("3. Căutare după IDNP");
        printer.printInfo("4. Căutare după nume");

        int searchType = reader.readIntInRange("Alegeți: ", 1, 4);

        try {
            switch (searchType) {
                case 1:
                    String id = reader.readString("ID Client: ");
                    Customer customer = customerService.findCustomerById(id);
                    displayCustomerDetails(customer);
                    break;
                case 2:
                    String email = reader.readEmail("Email: ");
                    Customer customerByEmail = customerService.findCustomerByEmail(email);
                    displayCustomerDetails(customerByEmail);
                    break;
                case 3:
                    String idnp = reader.readString("IDNP: ");
                    Customer customerByIdnp = customerService.findCustomerByIdentityNumber(idnp);
                    displayCustomerDetails(customerByIdnp);
                    break;
                case 4:
                    String searchTerm = reader.readString("Nume/Prenume: ");
                    List<Customer> customers = customerService.searchCustomers(searchTerm);
                    printer.printCustomers(customers);
                    break;
            }
        } catch (Exception e) {
            printer.printError("Client negăsit: " + e.getMessage());
        }
    }

    private void addCustomer() {
        printer.printSection("ADĂUGARE CLIENT NOU");

        String firstName = reader.readString("Prenume: ");
        String lastName = reader.readString("Nume: ");
        String email = reader.readEmail("Email: ");
        String phone = reader.readPhoneNumber("Telefon: ");
        String birthDateStr = reader.readString("Data nașterii (AAAA-LL-ZZ): ");
        String identityNumber = reader.readString("IDNP (13 cifre): ");

        try {
            LocalDate birthDate = LocalDate.parse(birthDateStr);
            Customer customer = customerService.createCustomer(
                    firstName, lastName, email, phone, birthDate, identityNumber);

            printer.printSuccess("Client adăugat cu succes!");
            displayCustomerDetails(customer);
        } catch (DateTimeParseException e) {
            printer.printError("Format dată invalid!");
        } catch (DuplicateCustomerException e) {
            printer.printError("Clientul există deja: " + e.getMessage());
        } catch (ValidationException e) {
            printer.printError("Date invalide: " + e.getMessage());
        }
    }

    private void editCustomer() {
        printer.printSection("EDITARE CLIENT");

        String customerId = reader.readString("ID Client: ");

        try {
            Customer customer = customerService.findCustomerById(customerId);
            displayCustomerDetails(customer);

            if (!reader.confirm("Editați acest client?")) {
                printer.printInfo("Operațiune anulată.");
                return;
            }

            printer.printInfo("Lăsați câmpul gol pentru a păstra valoarea actuală.");

            String newFirstName = reader.readString("Prenume (" + customer.getFirstName() + "): ");
            String newLastName = reader.readString("Nume (" + customer.getLastName() + "): ");
            String newEmail = reader.readString("Email (" + customer.getEmail() + "): ");
            String newPhone = reader.readString("Telefon (" + customer.getPhoneNumber() + "): ");
            String newAddress = reader.readLine("Adresă: ");

            Customer updatedCustomer = customerService.updateCustomer(
                    customerId,
                    newFirstName.isEmpty() ? null : newFirstName,
                    newLastName.isEmpty() ? null : newLastName,
                    newEmail.isEmpty() ? null : newEmail,
                    newPhone.isEmpty() ? null : newPhone,
                    newAddress.isEmpty() ? null : newAddress
            );

            printer.printSuccess("Client actualizat cu succes!");
            displayCustomerDetails(updatedCustomer);

        } catch (Exception e) {
            printer.printError("Eroare la editare: " + e.getMessage());
        }
    }

    private void toggleCustomerStatus() {
        printer.printSection("ACTIVARE/DEZACTIVARE CLIENT");

        String customerId = reader.readString("ID Client: ");

        try {
            Customer customer = customerService.findCustomerById(customerId);
            displayCustomerDetails(customer);

            if (customer.isActive()) {
                if (reader.confirm("Dezactivați acest client?")) {
                    customerService.deactivateCustomer(customerId);
                    printer.printSuccess("Client dezactivat!");
                }
            } else {
                if (reader.confirm("Activați acest client?")) {
                    customerService.activateCustomer(customerId);
                    printer.printSuccess("Client activat!");
                }
            }
        } catch (Exception e) {
            printer.printError("Eroare: " + e.getMessage());
        }
    }

    private void displayCustomerStatistics() {
        printer.printSection("STATISTICI CLIENȚI");


        long total = customerService.getTotalCustomerCount();
        long active = customerService.getActiveCustomerCount();

        printer.printInfo("Total clienți: " + total);
        printer.printInfo("Clienți activi: " + active);
        printer.printInfo("Clienți inactivi: " + (total - active));
        printer.newLine();

        // Clienți noi în ultimele 30 zile
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Customer> newCustomers = customerService.getCustomersBornAfter(thirtyDaysAgo);
        printer.printInfo("Clienți noi (30 zile): " + newCustomers.size());
    }

    private void displayCustomerDetails(Customer customer) {
        printer.printSection("DETALII CLIENT");
        printer.printInfo("ID Client: " + customer.getCustomerId());
        printer.printInfo("Nume: " + customer.getFullName());
        printer.printInfo("Email: " + customer.getEmail());
        printer.printInfo("Telefon: " + customer.getPhoneNumber());
        printer.printInfo("Data nașterii: " + printer.formatDate(customer.getBirthDate()));
        printer.printInfo("IDNP: " + customer.getIdentityNumber());
        printer.printInfo("Status: " + (customer.isActive() ? "Activ" : "Inactiv"));
        printer.printInfo("Înregistrat: " + printer.formatDateTime(customer.getRegistrationDate()));
    }
}
