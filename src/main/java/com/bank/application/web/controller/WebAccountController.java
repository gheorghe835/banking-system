package com.bank.application.web.controller;

import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.AuthService;
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
    private final AuthService authService;

    public WebAccountController(AccountService accountService,
                                TransactionService transactionService,
                                ExchangeService exchangeService,
                                AuthService authService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.exchangeService = exchangeService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String account,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            //redirectAttributes.addFlashAttribute("error", "Trebuie să fii autentificat");
            return "redirect:/web/login";
        }

        try {
            Account accountObj = accountService.findAccount(accountNumber);
            model.addAttribute("account", accountObj);
            //model.addAttribute("ownerName",accountObj.getOwner().getFullName());
            return "client/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
            return "redirect:/web/login";
        }
    }

    @GetMapping("/deposit")
    public String depositPage(@RequestParam(required = false) String account,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        model.addAttribute("accountNumber", accountNumber);
        return "client/deposit";
    }

    /*@PostMapping("/deposit")
    public String deposit(@RequestParam(required = false) String account,
                          @RequestParam BigDecimal amount,
                          @RequestParam(defaultValue = "MDL") String currencyCode,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            Currency currency = Currency.fromCode(currencyCode);
            //Account updatedAccount = accountService.deposit(accountNumber,amount,currency);
            //session.setAttribute("account", updatedAccount);
            //redirectAttributes.addFlashAttribute("success", "Depunere reușită: " + amount + " " + currencyCode);
            accountService.withdraw(accountNumber, amount, currency);
            redirectAttributes.addFlashAttribute("success", "Retragere reușită: " + amount + " " + currencyCode);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la depunere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard?account=" + accountNumber;    }*/
    @PostMapping("/deposit")
    public String deposit(@RequestParam(required = false) String account,
                          @RequestParam BigDecimal amount,
                          @RequestParam(defaultValue = "MDL") String currencyCode,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.deposit(accountNumber, amount, currency);
            redirectAttributes.addFlashAttribute("success", "Depunere reușită: " + amount + " " + currencyCode);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la depunere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard?account=" + accountNumber;
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam(required = false) String account,  // ← TREBUIE SĂ FIE "account"
                           @RequestParam BigDecimal amount,
                           @RequestParam(defaultValue = "MDL") String currencyCode,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        System.out.println("=== WITHDRAW ===");
        System.out.println("Account din formular: " + account);
        System.out.println("Amount: " + amount);
        System.out.println("Currency: " + currencyCode);

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            System.out.println("❌ Account number null!");
            return "redirect:/web/login";
        }

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.withdraw(accountNumber, amount, currency);
            redirectAttributes.addFlashAttribute("success", "Retragere reușită: " + amount + " " + currencyCode);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la retragere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard?account=" + accountNumber;
    }

    @GetMapping("/withdraw")
    public String withdrawPage(@RequestParam(required = false) String account,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        // Poți adăuga accountNumber în model dacă e nevoie în template
        model.addAttribute("accountNumber", accountNumber);

        return "client/withdraw";
    }

    /*@PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
                           @RequestParam(defaultValue = "MDL") String currencyCode,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String accountNumber = (String) session.getAttribute("accountNumber");

        try {
            Currency currency = Currency.fromCode(currencyCode);
            Account updatedAccount = accountService.withdraw(accountNumber,amount,currency);
            session.setAttribute("account", updatedAccount);
            redirectAttributes.addFlashAttribute("success", "Retragere reușită: " + amount + " " + currencyCode);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la retragere: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }*/

    @GetMapping("/transfer")
    public String transferPage(@RequestParam(required = false) String account,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        model.addAttribute("accountNumber", accountNumber);
        return "client/transfer";
    }

    /*
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
            Account updatedAccount = accountService.findAccount(sourceAccount);
            session.setAttribute("account", updatedAccount);
            redirectAttributes.addFlashAttribute("success",
                    "Transfer reușit: " + amount + " " + currencyCode + " către " + targetAccount);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la transfer: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard";
    }*/
    @PostMapping("/transfer")
    public String transfer(@RequestParam(required = false) String sourceAccount,
                           @RequestParam String targetAccount,
                           @RequestParam BigDecimal amount,
                           @RequestParam(defaultValue = "MDL") String currencyCode,
                           @RequestParam(required = false) String description,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        String source = sourceAccount != null ? sourceAccount : (String) session.getAttribute("accountNumber");

        if (source == null) {
            return "redirect:/web/login";
        }

        try {
            Currency currency = Currency.fromCode(currencyCode);
            accountService.transfer(source, targetAccount, amount, currency, description);
            redirectAttributes.addFlashAttribute("success",
                    "Transfer reușit: " + amount + " " + currencyCode + " către " + targetAccount);
        } catch (InsufficientFundsException e) {
            redirectAttributes.addFlashAttribute("error", "Fonduri insuficiente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la transfer: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard?account=" + source;
    }

    @GetMapping("/history")
    public String history(@RequestParam(required = false) String account,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            var transactions = transactionService.getAccountTransactions(accountNumber);
            model.addAttribute("transactions", transactions);
            model.addAttribute("accountNumber", accountNumber);
            return "client/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la încărcarea istoricului");
            return "redirect:/web/client/dashboard?account=" + accountNumber;
        }
    }

    @GetMapping("/exchange")
    public String exchangePage(@RequestParam(required = false) String account,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        // Poți adăuga și soldurile în model dacă vrei să le afișezi
        try {
            Account accountObj = accountService.findAccount(accountNumber);
            model.addAttribute("balances", accountObj.getAllBalances());
        } catch (Exception e) {
            // Ignoră
        }

        model.addAttribute("accountNumber", accountNumber);
        return "client/exchange";
    }

    @PostMapping("/exchange")
    public String exchange(@RequestParam(required = false) String account,
                           @RequestParam String fromCurrency,
                           @RequestParam String toCurrency,
                           @RequestParam BigDecimal amount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        try {
            Currency from = Currency.fromCode(fromCurrency);
            Currency to = Currency.fromCode(toCurrency);

            accountService.exchangeCurrency(accountNumber, from, to, amount);

            redirectAttributes.addFlashAttribute("success",
                    "Schimb realizat cu succes: " + amount + " " + fromCurrency + " -> " + toCurrency);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la schimb valutar: " + e.getMessage());
        }

        return "redirect:/web/client/dashboard?account=" + accountNumber;
    }



    @PostMapping("/change-password")
    public String changePassword(@RequestParam(required = false) String account,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele noi nu coincid!");
            return "redirect:/web/client/change-password?account=" + accountNumber;
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Parola nouă trebuie să aibă minim 6 caractere!");
            return "redirect:/web/client/change-password?account=" + accountNumber;
        }

        try {
            authService.changePassword(accountNumber, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Parola a fost schimbată cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/client/change-password?account=" + accountNumber;
    }

    @GetMapping("/change-password")  // ← TREBUIE SĂ FIE GET
    public String changePasswordPage(@RequestParam(required = false) String account,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        String accountNumber = account != null ? account : (String) session.getAttribute("accountNumber");

        if (accountNumber == null) {
            return "redirect:/web/login";
        }

        // Adaugă numele clientului în model
        try {
            Account accountObj = accountService.findAccount(accountNumber);
            model.addAttribute("ownerName", accountObj.getOwner().getFullName());
        } catch (Exception e) {
            // Ignoră
        }

        model.addAttribute("accountNumber", accountNumber);
        return "client/change-password";  // ← RETURNEAZĂ PAGINA
    }
    /*@PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String accountNumber = (String) session.getAttribute("accountNumber");

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele noi nu coincid!");
            return "redirect:/web/client/change-password";
        }

        try {
            authService.changePassword(accountNumber, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Parola a fost schimbată cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare: " + e.getMessage());
        }

        return "redirect:/web/client/change-password";
    }*/
}