package com.bank.infrastructure.security;

import com.bank.domain.service.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementare a PasswordEncoder folosind BCrypt
 */
@Component
public class PasswordEncoderImpl implements PasswordEncoder {

    private static final int BCRYPT_COST = 12;
    private final BCryptPasswordEncoder bCryptEncoder;

    public PasswordEncoderImpl() {
        this.bCryptEncoder = new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @Override
    public String encode(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Parola nu poate fi null sau goală");
        }
        return bCryptEncoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return bCryptEncoder.matches(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValidHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.length() < 10) {
            return false;
        }
        return hashedPassword.startsWith("$2a$") ||
                hashedPassword.startsWith("$2b$") ||
                hashedPassword.startsWith("$2y$");
    }

    @Override
    public String generateSalt() {
        // BCryptPasswordEncoder generează salt intern
        return "$2a$" + BCRYPT_COST + "$" + generateRandomString(22);
    }

    @Override
    public String hashWithSalt(String plainPassword, String salt) {
        if (plainPassword == null || salt == null) {
            throw new IllegalArgumentException("Parola și salt-ul nu pot fi null");
        }
        // BCrypt include salt-ul în hash
        return encode(plainPassword);
    }

    @Override
    public long estimateHashTime() {
        long startTime = System.currentTimeMillis();
        encode("test_password");
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String encodeWithCost(String plainPassword, int costFactor) {
        if (costFactor < 4 || costFactor > 31) {
            throw new IllegalArgumentException("Factorul de cost trebuie să fie între 4 și 31");
        }
        BCryptPasswordEncoder customEncoder = new BCryptPasswordEncoder(costFactor);
        return customEncoder.encode(plainPassword);
    }

    @Override
    public int checkPasswordStrength(String password) {
        if (password == null) return 0;

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        if (password.length() >= 12) score++;

        return Math.min(score, 5);
    }

    @Override
    public String generateSecurePassword(int length) {
        if (length < 8) {
            length = 12;
        }

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=";
        String allChars = upper + lower + digits + special;

        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        // Asigură cel puțin un caracter din fiecare categorie
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Completează restul
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters, new SecureRandom());

        StringBuilder result = new StringBuilder();
        for (char c : characters) {
            result.append(c);
        }
        return result.toString();
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
