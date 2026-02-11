package com.bank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Proprietăți configurabile pentru securitate
 */
@Component
@ConfigurationProperties(prefix = "banking.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Password password = new Password();
    private BruteForce bruteForce = new BruteForce();

    // Getters and Setters
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Password getPassword() { return password; }
    public void setPassword(Password password) { this.password = password; }

    public BruteForce getBruteForce() { return bruteForce; }
    public void setBruteForce(BruteForce bruteForce) { this.bruteForce = bruteForce; }

    // Inner classes for nested properties
    public static class Jwt {
        private String secret = "default-secret-key-change-in-production";
        private Duration expiration = Duration.ofHours(24);
        private String issuer = "banking-system";

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public Duration getExpiration() { return expiration; }
        public void setExpiration(Duration expiration) { this.expiration = expiration; }

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }

    public static class Password {
        private int minLength = 8;
        private boolean requireUppercase = true;
        private boolean requireLowercase = true;
        private boolean requireDigits = true;
        private boolean requireSpecialChars = false;
        private int bcryptStrength = 12;

        // Getters and Setters
        public int getMinLength() { return minLength; }
        public void setMinLength(int minLength) { this.minLength = minLength; }

        public boolean isRequireUppercase() { return requireUppercase; }
        public void setRequireUppercase(boolean requireUppercase) { this.requireUppercase = requireUppercase; }

        public boolean isRequireLowercase() { return requireLowercase; }
        public void setRequireLowercase(boolean requireLowercase) { this.requireLowercase = requireLowercase; }

        public boolean isRequireDigits() { return requireDigits; }
        public void setRequireDigits(boolean requireDigits) { this.requireDigits = requireDigits; }

        public boolean isRequireSpecialChars() { return requireSpecialChars; }
        public void setRequireSpecialChars(boolean requireSpecialChars) { this.requireSpecialChars = requireSpecialChars; }

        public int getBcryptStrength() { return bcryptStrength; }
        public void setBcryptStrength(int bcryptStrength) { this.bcryptStrength = bcryptStrength; }
    }

    public static class BruteForce {
        private int maxAttempts = 5;
        private Duration lockDuration = Duration.ofMinutes(30);
        private Duration resetDuration = Duration.ofHours(24);

        // Getters and Setters
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

        public Duration getLockDuration() { return lockDuration; }
        public void setLockDuration(Duration lockDuration) { this.lockDuration = lockDuration; }

        public Duration getResetDuration() { return resetDuration; }
        public void setResetDuration(Duration resetDuration) { this.resetDuration = resetDuration; }
    }
}
