package com.bank.application.web.controller;

import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.ExchangeService;
import com.bank.domain.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/web/client")
public class WebAccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ExchangeService exchangeService;

    public WebAccountController(AccountService accountService,
                                TransactionService transactionService,
                                ExchangeService exchangeService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.exchangeService = exchangeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login";
        }

        try {
            Account account = accountService.findAccount(accountNumber);
            model.addAttribute("account", account);
            return "client/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
            return "redirect:/web/login";
        }
    }

    @GetMapping("/deposit")
    public String depositPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("accountNumber") == null) {
            return "redirect:/web/login";
        }
        return "client/deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount,
                          @RequestParam(defaultValue = "MDL") String currencyCode,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.deposit(accountNumber, amount, currency);
            redirectAttributes.addFlashAttribute("success", "Depunere reușită: " + amount + " " + currencyCode);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la depunere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }

    @GetMapping("/withdraw")
    public String withdrawPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("accountNumber") == null) {
            return "redirect:/web/login";
        }
        return "client/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
                           @RequestParam(defaultValue = "MDL") String currencyCode,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.withdraw(accountNumber, amount, currency);
            redirectAttributes.addFlashAttribute("success", "Retragere reușită: " + amount + " " + currencyCode);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la retragere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }

    @GetMapping("/transfer")
    public String transferPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("accountNumber") == null) {
            return "redirect:/web/login";
        }
        return "client/transfer";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String targetAccount,
                           @RequestParam BigDecimal amount,
                           @RequestParam(defaultValue = "MDL") String currencyCode,
                           @RequestParam(required = false) String description,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String sourceAccount = (String) session.getAttribute("accountNumber");

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.transfer(sourceAccount, targetAccount, amount, currency, description);
            redirectAttributes.addFlashAttribute("success",
                    "Transfer reușit: " + amount + " " + currencyCode + " către " + targetAccount);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la transfer: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            var transactions = transactionService.getAccountTransactions(accountNumber);
            model.addAttribute("transactions", transactions);
            return "client/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la încărcarea istoricului");
            return "redirect:/web/client/dashboard";
        }
    }

    @GetMapping("/exchange")
    public String exchangePage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            Account account = accountService.findAccount(accountNumber);
            model.addAttribute("balances", account.getAllBalances());
            return "client/exchange";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
            return "redirect:/web/client/dashboard";
        }
    }

    @PostMapping("/exchange")
    public String exchange(@RequestParam String fromCurrency,
                           @RequestParam String toCurrency,
                           @RequestParam BigDecimal amount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        String accountNumber = (String) session.getAttribute("accountNumber");

        try {
            Currency from = Currency.fromCode(fromCurrency);
            Currency to = Currency.fromCode(toCurrency);

            // Folosește ExchangeService pentru a calcula suma convertită
            BigDecimal convertedAmount = exchangeService.calculateExchange(amount, from, to);

            // Apelează serviciul de schimb valutar
            accountService.exchangeCurrency(accountNumber, from, to, amount);

            redirectAttributes.addFlashAttribute("success",
                    String.format("Schimb realizat cu succes: %.2f %s -> %.2f %s",
                            amount, fromCurrency, convertedAmount, toCurrency));

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la schimb valutar: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }
}