package com.bank.application.web.controller;

import com.bank.application.web.dto.LoginRequest;
import com.bank.domain.model.Account;
import com.bank.domain.model.BankManager;
import com.bank.domain.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/client/login")
    public ResponseEntity<Map<String, Object>> clientLogin(@RequestBody LoginRequest request) {
        Account account = authService.authenticateClient(request.getUsername(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("accountNumber", account.getAccountNumber());
        response.put("ownerName", account.getOwner().getFullName());
        response.put("accountType", account.getAccountType());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/manager/login")
    public ResponseEntity<Map<String, Object>> managerLogin(@RequestBody LoginRequest request) {
        BankManager manager = authService.authenticateManager(request.getUsername(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("employeeId", manager.getEmployeeId());
        response.put("name", manager.getFullName());
        response.put("accessLevel", manager.getAccessLevel());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Deconectare reușită");
        return ResponseEntity.ok(response);
    }
}