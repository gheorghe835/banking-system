package com.bank.application.web.controller;

import com.bank.domain.model.Transaction;
import com.bank.domain.model.Currency;
import com.bank.domain.service.*;
import com.bank.domain.model.BankManager;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/admin")
public class WebAdminController {

    private final AccountService accountService;
    private final CustomerService customerService;
    private final InterestService interestService;
    private final TransactionService transactionService;
    private final ExchangeService exchangeService;

    public WebAdminController(AccountService accountService,
                              CustomerService customerService,
                              InterestService interestService,
                              TransactionService transactionService,
                              ExchangeService exchangeService) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.interestService = interestService;
        this.transactionService = transactionService;
        this.exchangeService = exchangeService;
    }

    @GetMapping("/management")
    public String management(HttpSession session, RedirectAttributes redirectAttributes) {
        BankManager manager = (BankManager) session.getAttribute("manager");
        if (manager == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }
        return "admin/management";
    }

    @GetMapping("/accounts")
    @Transactional
    public String listAccounts(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "admin/accounts";
    }

    @GetMapping("/customers")
    public String listCustomers(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }

    @GetMapping("/reports/balance")
    public String balanceReport(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        model.addAttribute("totalAccounts", accountService.getTotalAccountCount());
        model.addAttribute("activeAccounts", accountService.getActiveAccountCount());
        model.addAttribute("totalBalance", accountService.getTotalBankBalance());
        model.addAttribute("balancePerCurrency", accountService.getTotalBalancePerCurrency());

        return "admin/balance-report";
    }

    @PostMapping("/interest/apply")
    public String applyInterest(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            interestService.applyInterestToAllAccounts();
            redirectAttributes.addFlashAttribute("success", "Dobânda a fost aplicată cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la aplicarea dobânzii: " + e.getMessage());
        }

        return "redirect:/web/admin/management";
    }

    private boolean isManagerAuthenticated(HttpSession session) {
        return session.getAttribute("manager") != null;
    }

    @GetMapping("/accounts/create")
    public String createAccountPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        BankManager manager = (BankManager) session.getAttribute("manager");
        if (manager == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        // 🔴 LINIA IMPORTANTĂ - trimite lista de clienți la pagină
        List<Customer> customers = customerService.getActiveCustomers();
        model.addAttribute("customers", customers);

        return "admin/accounts-create";
    }

    @PostMapping("/accounts/create")
    public String createAccount(@RequestParam(required = false) String clientType,
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
                                RedirectAttributes redirectAttributes) {
        try {
            Customer customer;

            // Scenariul 1: Client existent
            if ("existing".equals(clientType) && customerId != null && !customerId.isEmpty()) {
                customer = customerService.findCustomerById(customerId);
            }
            // Scenariul 2: Client nou
            else {
                LocalDate birthDateParsed = birthDate != null && !birthDate.isEmpty() ?
                        LocalDate.parse(birthDate) : null;

                customer = customerService.createCustomer(
                        firstName, lastName, email, phoneNumber,
                        birthDateParsed, identityNumber
                );

                if (address != null && !address.isEmpty()) {
                    customer.setAddress(address);
                    customerService.updateCustomer(customer.getCustomerId(),
                            null, null, null, null, address);
                }
            }

            // Creează contul
            accountService.createAccount(accountNumber, customer, accountType, initialBalance);
            redirectAttributes.addFlashAttribute("success", "Cont creat cu succes!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }
        return "redirect:/web/admin/accounts";
    }

    @GetMapping("/customers/{id}")
    @Transactional
    public String customerDetails(@PathVariable String id, Model model, HttpSession session) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        Customer customer = customerService.findCustomerById(id);
        List<Account> accounts = accountService.getCustomerAccounts(id);

        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accounts);

        return "admin/customer-details";
    }

    @GetMapping("/customers/edit/{id}")
    public String editCustomerPage(@PathVariable String id, Model model, HttpSession session) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        Customer customer = customerService.findCustomerById(id);
        model.addAttribute("customer", customer);

        return "admin/customer-edit";
    }

    @PostMapping("/customers/update")
    public String updateCustomer(@RequestParam String customerId,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String phoneNumber,
                                 @RequestParam(required = false) String address,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.updateCustomer(customerId, firstName, lastName, email, phoneNumber, address);
            redirectAttributes.addFlashAttribute("success", "Client actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }
        return "redirect:/web/admin/customers";
    }

    @GetMapping("/customers/toggle/{id}")
    public String toggleCustomerStatus(@PathVariable String id,
                                       RedirectAttributes redirectAttributes,
                                       HttpSession session) {

        // Verifică autentificarea
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            Customer customer = customerService.findCustomerById(id);

            if (customer.isActive()) {
                customerService.deactivateCustomer(id);
                redirectAttributes.addFlashAttribute("success",
                        "Clientul " + customer.getFullName() + " a fost DEZACTIVAT cu succes!");
            } else {
                customerService.activateCustomer(id);
                redirectAttributes.addFlashAttribute("success",
                        "Clientul " + customer.getFullName() + " a fost ACTIVAT cu succes!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Eroare la modificarea statusului: " + e.getMessage());
        }

        return "redirect:/web/admin/customers";
    }
    // Detalii cont
    @GetMapping("/accounts/{accountNumber}")
    @Transactional
    public String accountDetails(@PathVariable String accountNumber,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            Account account = accountService.findAccount(accountNumber);
            model.addAttribute("account", account);
            return "admin/account-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cont negăsit: " + e.getMessage());
            return "redirect:/web/admin/accounts";
        }
    }

    // Blocare cont
    @GetMapping("/accounts/block/{accountNumber}")
    public String blockAccount(@PathVariable String accountNumber,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            accountService.blockAccount(accountNumber);
            redirectAttributes.addFlashAttribute("success", "Contul " + accountNumber + " a fost blocat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la blocare: " + e.getMessage());
        }

        return "redirect:/web/admin/accounts";
    }

    // Deblocare cont
    @GetMapping("/accounts/unblock/{accountNumber}")
    public String unblockAccount(@PathVariable String accountNumber,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            accountService.unblockAccount(accountNumber);
            redirectAttributes.addFlashAttribute("success", "Contul " + accountNumber + " a fost deblocat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la deblocare: " + e.getMessage());
        }

        return "redirect:/web/admin/accounts";
    }

    @GetMapping("/customers/create")
    public String createCustomerPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        // Verifică autentificarea
        BankManager manager = (BankManager) session.getAttribute("manager");
        if (manager == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login?manager=true";
        }

        return "admin/customers-create";  // ← trebuie să existe acest template
    }

    @GetMapping("/reports/transactions")
    @Transactional(readOnly = true)
    public String transactionsReport(@RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(required = false) String accountNumber,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        try {
            LocalDateTime start = startDate != null ?
                    LocalDate.parse(startDate).atStartOfDay() :
                    LocalDate.now().minusMonths(1).atStartOfDay();

            LocalDateTime end = endDate != null ?
                    LocalDate.parse(endDate).plusDays(1).atStartOfDay() :
                    LocalDateTime.now();

            List<com.bank.domain.model.Transaction> transactions;  // ← specifică fully qualified name

            if (accountNumber != null && !accountNumber.isEmpty()) {
                transactions = transactionService.getAccountTransactions(accountNumber)
                        .stream()
                        .filter(t -> t.getTimestamp().isAfter(start) && t.getTimestamp().isBefore(end))
                        .collect(Collectors.toList());
            } else {
                transactions = transactionService.getTransactionsBetween(start, end);
            }

            BigDecimal totalAmount = transactions.stream()
                    .filter(t -> t.getStatus() == com.bank.domain.model.Transaction.TransactionStatus.COMPLETED)
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
            return "redirect:/web/admin/management";
        }

        return "admin/transactions-report";
    }

    @GetMapping("/exchange-rates")
    public String exchangeRatesPage(Model model, HttpSession session) {
        if (!isManagerAuthenticated(session)) {
            return "redirect:/web/login?manager=true";
        }

        Map<Currency, BigDecimal> rates = exchangeService.getAllExchangeRates();
        model.addAttribute("rates", rates);

        return "admin/exchange-rates";
    }

    @PostMapping("/exchange-rates/update")
    public String updateExchangeRates(@RequestParam(required = false) BigDecimal rateEUR,
                                      @RequestParam(required = false) BigDecimal rateUSD,
                                      @RequestParam(required = false) BigDecimal rateGBP,
                                      @RequestParam(required = false) BigDecimal rateRON,
                                      RedirectAttributes redirectAttributes) {

        try {
            if (rateEUR != null) exchangeService.updateExchangeRate(Currency.EUR, rateEUR);
            if (rateUSD != null) exchangeService.updateExchangeRate(Currency.USD, rateUSD);
            if (rateGBP != null) exchangeService.updateExchangeRate(Currency.GBP, rateGBP);
            if (rateRON != null) exchangeService.updateExchangeRate(Currency.RON, rateRON);

            redirectAttributes.addFlashAttribute("success", "Cursurile valutare au fost actualizate!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/admin/exchange-rates";
    }


}
