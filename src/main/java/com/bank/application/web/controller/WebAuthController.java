package com.bank.application.web.controller;

import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

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
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) boolean manager,
                        HttpSession session,
                        HttpServletRequest request,
                        Model model) {
        try {
            System.out.println("=== ÎNCERCARE AUTENTIFICARE ===");
            System.out.println("Username: " + username);
            System.out.println("Manager: " + manager);

            if (manager) {
                BankManager bankManager = authService.authenticateManager(username, password);
                session.setAttribute("user", username);
                session.setAttribute("userType", "MANAGER");
                return "redirect:/web/admin/management?user=" + username;

            } else {
                Account account = authService.authenticateClient(username, password);
                session.setAttribute("user", account.getAccountNumber());
                session.setAttribute("userType", "CLIENT");
                return "redirect:/web/client/dashboard?account=" + account.getAccountNumber();
            }
        } catch (Exception e) {
            System.out.println("❌ EROARE LA AUTENTIFICARE: " + e.getMessage());
            e.printStackTrace();
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
