package com.bank.application.web.controller;

import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/web")
public class WebAuthController {

    private final AuthService authService;
    private final AccountService accountService;

    public WebAuthController(AuthService authService, AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) boolean manager, Model model) {
        model.addAttribute("manager", manager);
        return "login";
    }

    @PostMapping("/login")
    @Transactional
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) boolean manager,
                        HttpSession session,
                        Model model) {
        try {
            if (manager) {
                BankManager bankManager = authService.authenticateManager(username, password);
                session.setAttribute("manager", bankManager);
                session.setAttribute("userType", "MANAGER");
                return "redirect:/web/admin/management";
            } else {
                Account account = authService.authenticateClient(username, password);
                account.getOwner().getFullName();
                session.setAttribute("account", account);
                session.setAttribute("accountNumber", account.getAccountNumber());
                session.setAttribute("userType", "CLIENT");
                return "redirect:/web/client/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Autentificare eșuată: " + e.getMessage());
            model.addAttribute("manager", manager);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/web/";
    }
}
