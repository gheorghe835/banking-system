package com.bank.domain.service;

/**
 * Interfață pentru encoding-ul și verificarea parolelor
 */
public interface PasswordEncoder {

    /**
     * Hash-uieste o parolă plaintext
     */
    String encode(String plainPassword);

    /**
     * Verifică dacă o parolă plaintext corespunde cu hash-ul
     */
    boolean matches(String plainPassword, String hashedPassword);

    /**
     * Verifică dacă un hash este valid
     */
    boolean isValidHash(String hashedPassword);

    /**
     * Generează un salt
     */
    String generateSalt();

    /**
     * Hash-uieste o parolă cu un salt specific
     */
    String hashWithSalt(String plainPassword, String salt);

    /**
     * Estimează timpul de hash
     */
    long estimateHashTime();

    /**
     * Hash-uieste cu cost factor specific
     */
    String encodeWithCost(String plainPassword, int costFactor);

    /**
     * Verifică puterea parolei
     */
    int checkPasswordStrength(String password);

    /**
     * Generează o parolă securizată
     */
    String generateSecurePassword(int length);
}
