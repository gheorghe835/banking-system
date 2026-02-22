package com.bank.application.web.controller;

import com.bank.application.web.dto.AccountDTO;
import com.bank.domain.model.Transaction;
import com.bank.domain.model.Currency;
import com.bank.domain.service.*;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Account;
import com.bank.domain.repository.AccountRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/admin")
public class WebAdminController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final InterestService interestService;
    private final TransactionService transactionService;
    private final ExchangeService exchangeService;
    private final AuthService authService;


    public WebAdminController(AccountService accountService,
                              CustomerService customerService,
                              InterestService interestService,
                              TransactionService transactionService,
                              ExchangeService exchangeService,
                              AuthService authService) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.interestService = interestService;
        this.transactionService = transactionService;
        this.exchangeService = exchangeService;
        this.authService = authService;
    }

    @GetMapping("/management")
    public String management(@RequestParam(required = false) String user,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        try {
            BankManager manager = authService.findManagerByUsername(user);
            model.addAttribute("managerName", manager.getFullName());
            model.addAttribute("managerLevel", manager.getAccessLevel().toString());
            return "admin/management";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la încărcarea datelor");
            return "redirect:/web/login?manager=true";
        }
    }

    @GetMapping("/accounts")
    @Transactional
    public String listAccounts(@RequestParam(required = false) String user,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {



        System.out.println("=== LIST ACCOUNTS CALLED ===");
        System.out.println("User param: " + user);
        System.out.println("Session ID: " + session.getId());

        if (user == null) {
            System.out.println(" User is null, redirecting to login");
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        List<Account> accounts = accountService.getAllAccounts();

        List<AccountDTO> accountDTOs = accounts.stream()
                .map(account -> {
                    AccountDTO dto = new AccountDTO();
                    dto.setAccountNumber(account.getAccountNumber());
                    dto.setAccountType(account.getAccountType());
                    dto.setActive(account.isActive());
                    dto.setOwnerName(account.getOwner() != null ? account.getOwner().getFullName() : "N/A");

                    Map<String, BigDecimal> balances = new HashMap<>();
                    balances.put("MDL", account.getBalance(Currency.MDL));
                    dto.setBalances(balances);

                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("=== ACCOUNT DTOs ===");
        accountDTOs.forEach(dto -> {
            System.out.println("Cont: " + dto.getAccountNumber());
            System.out.println("Owner: " + dto.getOwnerName());
            System.out.println("Balances: " + dto.getBalances());
        });

        model.addAttribute("accounts", accountDTOs);
        return "admin/accounts";
    }

    @GetMapping("/customers")
    public String listCustomers(@RequestParam(required = false) String user,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }

    @GetMapping("/reports/balance")
    public String balanceReport(@RequestParam(required = false) String user,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        model.addAttribute("totalAccounts", accountService.getTotalAccountCount());
        model.addAttribute("activeAccounts", accountService.getActiveAccountCount());
        model.addAttribute("totalBalance", accountService.getTotalBankBalance());
        model.addAttribute("balancePerCurrency", accountService.getTotalBalancePerCurrency());

        return "admin/balance-report";
    }

    @PostMapping("/interest/apply")
    public String applyInterest(@RequestParam(required = false) String user,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        try {
            interestService.applyInterestToAllAccounts();
            redirectAttributes.addFlashAttribute("success", "Dobânda a fost aplicată cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la aplicarea dobânzii: " + e.getMessage());
        }

        return "redirect:/web/admin/management?user=" + user;
    }


    @GetMapping("/accounts/create")
    public String createAccountPage(@RequestParam(required = false) String user,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        String generatedAccountNumber = generateUniqueAccountNumber();
        model.addAttribute("generatedAccountNumber", generatedAccountNumber);

        List<Customer> customers = customerService.getActiveCustomers();
        model.addAttribute("customers", customers);

        return "admin/accounts-create";
    }
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = new Random().ints(0, 10)
                    .limit(16)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.joining());
        } while (accountService.existsByAccountNumber(accountNumber));
        return accountNumber;
    }



    @PostMapping("/customers/create")
    public String createCustomer(@RequestParam(required = false) String user,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String phoneNumber,
                                 @RequestParam String birthDate,
                                 @RequestParam String identityNumber,
                                 @RequestParam(required = false) String address,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        try {
            LocalDate birthDateParsed = LocalDate.parse(birthDate);

            Customer customer = customerService.createCustomer(
                    firstName, lastName, email, phoneNumber, birthDateParsed, identityNumber
            );

            if (address != null && !address.isEmpty()) {
                customer.setAddress(address);
                customerService.updateCustomer(customer.getCustomerId(), null, null, null, null, address);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Client adăugat cu succes! ID: " + customer.getCustomerId());

            System.out.println("✅ Client creat: " + customer.getFullName() + " (" + customer.getCustomerId() + ")");

        } catch (Exception e) {
            System.out.println(" EROARE LA CREARE CLIENT: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/admin/customers?user=" + user;
    }

    @GetMapping("/customers/{id}")
    @Transactional
    public String customerDetails(@PathVariable String id,
                                  @RequestParam(required = false) String user,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        Customer customer = customerService.findCustomerById(id);
        List<Account> accounts = accountService.getCustomerAccounts(id);

        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "admin/customer-details";
    }

    @GetMapping("/customers/edit/{id}")
    public String editCustomerPage(@PathVariable String id,
                                   @RequestParam(required = false) String user,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("customer", customer);

        return "admin/customer-edit";
    }

    @PostMapping("/customers/update")
    public String updateCustomer(@RequestParam(required = false) String user,
                                 @RequestParam String customerId,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String phoneNumber,
                                 @RequestParam(required = false) String address,
                                 RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        try {
            customerService.updateCustomer(customerId, firstName, lastName, email, phoneNumber, address);
            redirectAttributes.addFlashAttribute("success", "Client actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }
        return "redirect:/web/admin/customers?user=" + user;
    }

    @GetMapping("/customers/toggle/{id}")
    public String toggleCustomerStatus(@PathVariable String id,
                                       @RequestParam(required = false) String user,
                                       RedirectAttributes redirectAttributes,
                                       HttpSession session) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        try {
            Customer customer = customerService.findCustomerById(id);
            String oldStatus = customer.isActive() ? "ACTIV" : "INACTIV";

            if (customer.isActive()) {
                customerService.deactivateCustomer(id);
                redirectAttributes.addFlashAttribute("success",
                        "Clientul " + customer.getFullName() + " a fost DEZACTIVAT cu succes!");
            } else {
                customerService.activateCustomer(id);
                redirectAttributes.addFlashAttribute("success",
                        "Clientul " + customer.getFullName() + " a fost ACTIVAT cu succes!");
            }

            System.out.println(" Status schimbat pentru clientul " + id +
                    ": " + oldStatus + " -> " + (customer.isActive() ? "ACTIV" : "INACTIV"));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/admin/customers?user=" + user;
    }
    @GetMapping("/accounts/{accountNumber}")
    @Transactional
    public String accountDetails(@PathVariable String accountNumber,
                                 @RequestParam(required = false) String user,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        try {
            Account account = accountService.findAccount(accountNumber);
            model.addAttribute("account", account);
            return "admin/account-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cont negăsit: " + e.getMessage());
            return "redirect:/web/admin/accounts?user=" + user;
        }
    }

    @GetMapping("/accounts/block/{accountNumber}")
    public String blockAccount(@PathVariable String accountNumber,
                               @RequestParam(required = false) String user,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        System.out.println("=== BLOCK ACCOUNT ===");
        System.out.println("Account: " + accountNumber);
        System.out.println("User: " + user);

        if (user == null) {
            System.out.println(" User is null, redirecting to login");
            return "redirect:/web/login?manager=true";
        }

        try {
            accountService.blockAccount(accountNumber);
            redirectAttributes.addFlashAttribute("success", "Contul " + accountNumber + " a fost blocat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la blocare: " + e.getMessage());
        }

        return "redirect:/web/admin/accounts?user=" + user;
    }

    @GetMapping("/accounts/unblock/{accountNumber}")
    public String unblockAccount(@PathVariable String accountNumber,
                                 @RequestParam(required = false) String user,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        try {
            accountService.unblockAccount(accountNumber);
            redirectAttributes.addFlashAttribute("success", "Contul " + accountNumber + " a fost deblocat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la deblocare: " + e.getMessage());
        }

        return "redirect:/web/admin/accounts?user=" + user;
    }



    @GetMapping("/reports/transactions")
    @Transactional(readOnly = true)
    public String transactionsReport(@RequestParam(required = false) String user,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(required = false) String accountNumber,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        try {
            LocalDateTime start = startDate != null ?
                    LocalDate.parse(startDate).atStartOfDay() :
                    LocalDate.now().minusMonths(1).atStartOfDay();

            LocalDateTime end = endDate != null ?
                    LocalDate.parse(endDate).plusDays(1).atStartOfDay() :
                    LocalDateTime.now();

            List<Transaction> transactions;

            if (accountNumber != null && !accountNumber.isEmpty()) {
                transactions = transactionService.getAccountTransactions(accountNumber)
                        .stream()
                        .filter(t -> t.getTimestamp().isAfter(start) && t.getTimestamp().isBefore(end))
                        .collect(Collectors.toList());
            } else {
                transactions = transactionService.getTransactionsBetween(start, end);
            }

            BigDecimal totalAmount = transactions.stream()
                    .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                    .map(t -> {
                        if (t.getCurrency() == Currency.MDL) {
                            return t.getAmount();
                        } else {
                            return t.getAmount().multiply(
                                    BigDecimal.valueOf(t.getCurrency().getExchangeRateToMDL())
                            );
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("transactions", transactions);
            model.addAttribute("totalCount", transactions.size());
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("startDate", start.toLocalDate());
            model.addAttribute("endDate", end.toLocalDate());
            model.addAttribute("accountNumber", accountNumber);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la generarea raportului: " + e.getMessage());
            return "redirect:/web/admin/management?user=" + user;
        }

        return "admin/transactions-report";
    }

    @GetMapping("/exchange-rates")
    public String exchangeRatesPage(@RequestParam(required = false) String user,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        Map<Currency, BigDecimal> rates = exchangeService.getAllExchangeRates();
        model.addAttribute("rates", rates);

        return "admin/exchange-rates";
    }

    @PostMapping("/exchange-rates/update")
    public String updateExchangeRates(@RequestParam(required = false) String user,
                                      @RequestParam(required = false) BigDecimal rateEUR,
                                      @RequestParam(required = false) BigDecimal rateUSD,
                                      @RequestParam(required = false) BigDecimal rateGBP,
                                      @RequestParam(required = false) BigDecimal rateRON,
                                      RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        try {
            if (rateEUR != null) exchangeService.updateExchangeRate(Currency.EUR, rateEUR);
            if (rateUSD != null) exchangeService.updateExchangeRate(Currency.USD, rateUSD);
            if (rateGBP != null) exchangeService.updateExchangeRate(Currency.GBP, rateGBP);
            if (rateRON != null) exchangeService.updateExchangeRate(Currency.RON, rateRON);

            redirectAttributes.addFlashAttribute("success", "Cursurile valutare au fost actualizate!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/admin/exchange-rates?user=" + user;
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@RequestParam(required = false) String user,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }
        return "admin/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam(required = false) String user,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele noi nu coincid!");
            return "redirect:/web/admin/change-password?user=" + user;
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Parola nouă trebuie să aibă minim 6 caractere!");
            return "redirect:/web/admin/change-password?user=" + user;
        }

        try {
            redirectAttributes.addFlashAttribute("success", "Parola a fost schimbată cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/admin/change-password?user=" + user;
    }

    private boolean isManagerAuthenticated(HttpSession session) {
        return false; // Nu mai folosim această metodă
    }

    @GetMapping("/customers/create")
    public String createCustomerPage(@RequestParam(required = false) String user,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }
        return "admin/customers-create";
    }

    @PostMapping("/accounts/create")
    public String createAccount(@RequestParam(required = false) String user,
                                @RequestParam(required = false) String clientType,
                                @RequestParam(required = false) String customerId,
                                @RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String lastName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam(required = false) String birthDate,
                                @RequestParam(required = false) String identityNumber,
                                @RequestParam(required = false) String address,
                                @RequestParam String accountNumber,
                                @RequestParam String accountType,
                                @RequestParam BigDecimal initialBalance,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {

        // Verifică autentificarea managerului
        if (user == null) {
            return "redirect:/web/login?manager=true";
        }

        // Validare parole
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele nu coincid!");
            return "redirect:/web/admin/accounts/create?user=" + user;
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Parola trebuie să aibă minim 6 caractere!");
            return "redirect:/web/admin/accounts/create?user=" + user;
        }

        try {
            Customer customer;

            // Cazul 1: Client existent
            if ("existing".equals(clientType) && customerId != null && !customerId.isEmpty()) {
                customer = customerService.findCustomerById(customerId);
            }
            // Cazul 2: Client nou
            else {
                // Validare date client nou
                if (firstName == null || lastName == null || email == null ||
                        phoneNumber == null || birthDate == null || identityNumber == null) {
                    redirectAttributes.addFlashAttribute("error", "Toate câmpurile pentru client nou sunt obligatorii!");
                    return "redirect:/web/admin/accounts/create?user=" + user;
                }

                LocalDate birthDateParsed = LocalDate.parse(birthDate);

                customer = customerService.createCustomer(
                        firstName, lastName, email, phoneNumber, birthDateParsed, identityNumber
                );

                if (address != null && !address.isEmpty()) {
                    customer.setAddress(address);
                    customerService.updateCustomer(customer.getCustomerId(), null, null, null, null, address);
                }

                System.out.println("✅ Client salvat cu ID: " + customer.getCustomerId());
            }

            // Creează contul
            Account account = accountService.createAccount(
                    accountNumber, customer, accountType, initialBalance, password
            );

            redirectAttributes.addFlashAttribute("success",
                    "✅ Cont creat cu succes pentru clientul " + customer.getFullName() +
                            " (Cont: " + accountNumber + ")");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Eroare la crearea contului: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/web/admin/accounts?user=" + user;
    }


}